package com.teamtiger.productservice;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.reservations.ClaimCodeGenerator;
import com.teamtiger.productservice.reservations.entities.ClaimCode;
import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.exceptions.BundleAlreadyReservedException;
import com.teamtiger.productservice.reservations.exceptions.ReservationNotFoundException;
import com.teamtiger.productservice.reservations.models.*;
import com.teamtiger.productservice.reservations.repositories.ClaimCodeRepository;
import com.teamtiger.productservice.reservations.repositories.ReservationRepository;
import com.teamtiger.productservice.reservations.services.ReservationEventPublisher;
import com.teamtiger.productservice.reservations.services.ReservationServiceJPA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


/**
 * Unit tests for the reservation service
 */
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BundleRepository bundleRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private ClaimCodeGenerator claimCodeGenerator;

    @Mock
    private ClaimCodeRepository claimCodeRepository;


    @InjectMocks
    private ReservationServiceJPA reservationService;

    private UUID testReservationId;
    private UUID testBundleId;
    private UUID testUserId;
    private UUID testVendorId;

    private Bundle mockBundle;
    private Reservation mockReservation;
    private ClaimCode mockClaimCode;


    @BeforeEach
    void set_up() {

        testReservationId = UUID.randomUUID();
        testBundleId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testVendorId = UUID.randomUUID();

        mockBundle = Bundle.builder()
                .id(testBundleId)
                .name("Sweet Treat Bundle")
                .description("test bundle desc")
                .price(4.71)
                .retailPrice(12.40)
                .vendorId(testVendorId)
                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .collectionStart(LocalDateTime.now().plusHours(1))
                .collectionEnd(LocalDateTime.now().plusHours(3))
                .allergies(Set.of())
                .bundleProducts(new HashSet<>()) //hashset so its immutable for addProducts
                .postingTime(LocalDateTime.now())
                .build();



        mockReservation = Reservation.builder()
                .id(testReservationId)
                .bundle(mockBundle)
                .userId(testUserId)
                .amountDue(4.71)
                .status(CollectionStatus.RESERVED)
                .timeReserved(LocalDateTime.now())
                .build();

        mockClaimCode = ClaimCode.builder()
                .reservation(mockReservation)
                .claimCode("ABC123")
                .build();
    }


    /**
     * createReservation test 1
     * success
     * returns ReservationDTO with correct fields
     */
    @Test
    public void testCreateReservation_Success() {


        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.existsByBundleId(testBundleId)).thenReturn(false);
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.of(mockBundle));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);
        when(claimCodeGenerator.generateCode()).thenReturn("ABC123");
        when(claimCodeRepository.save(any(ClaimCode.class))).thenReturn(mockClaimCode);

        ReservationDTO result = reservationService.createReservation(testBundleId, "Bearer userAccessToken123");

        assertThat(result).isNotNull();

        assertThat(result.getReservationId()).isEqualTo(testReservationId);

        //ensure that reservation and claim code were there
        verify(reservationRepository).save(any(Reservation.class));
        verify(claimCodeRepository).save(any(ClaimCode.class));

    }


    /**
     * createReservation test 2
     * a non user token tries to make a reservation
     * AuthorizationException thrown
     */
    @Test
    public void testCreateReservation_NonUser() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> reservationService.createReservation(testBundleId, "Bearer vendorAccessToken123"))
                .isInstanceOf(AuthorizationException.class);

        //make sure we never touch the database if auth fails
        verify(reservationRepository, never()).save(any(Reservation.class));

    }


    /**
     * createReservation test 3
     * bundle is already reserved
     * BundleAlreadyReservedException thrown
     */
    @Test
    public void testCreateReservation_BundleAlreadyReserved() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //bundle is already taken
        when(reservationRepository.existsByBundleId(testBundleId)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(testBundleId, "Bearer userAccessToken123"))
                .isInstanceOf(BundleAlreadyReservedException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    /**
     * createReservation test 4
     * bundle doesnt exist in the db
     * BundleNotFoundException thrown
     */
    @Test
    public void testCreateReservation_BundleNotFound() {


        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.existsByBundleId(testBundleId)).thenReturn(false);

        //bundle cannot be found
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(testBundleId, "Bearer userAccessToken123"))
                .isInstanceOf(BundleNotFoundException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));

    }


    /**
     * getReservations test 1
     * success
     * returns only pending (non expired) reservations
     */
    @Test
    public void testGetReservations_Success() {

        //collection window is still open
        // this reservation should be returned
        Reservation pendingReservation = Reservation.builder()
                .id(testReservationId)
                .bundle(mockBundle) //3 hours from now
                .userId(testUserId)
                .amountDue(4.71)
                .status(CollectionStatus.RESERVED)
                .timeReserved(LocalDateTime.now())
                .build();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findAllByUserIdAndStatus(testUserId, CollectionStatus.RESERVED))
                .thenReturn(List.of(pendingReservation));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of());

        List<ReservationDTO> result = reservationService.getReservations("Bearer userAccessToken123", CollectionStatus.RESERVED);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(testReservationId);
    }


    /**
     * getReservations test 2
     * expired reservations are filtered
     * and marked as NO_SHOW
     */
    @Test
    public void testGetReservations_ExpiredReservationsFiltered() {

        //collection window already closed
        // should be marked NO_SHOW and filter out
        Bundle expiredBundle = Bundle.builder()
                .id(UUID.randomUUID())
                .name("Expired Bundle")
                .collectionStart(LocalDateTime.now().minusHours(4))
                .collectionEnd(LocalDateTime.now().minusHours(1)) //already past
                .price(2.00)
                .vendorId(testVendorId)
                .build();


        Reservation expiredReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .bundle(expiredBundle)
                .userId(testUserId)
                .amountDue(2.00)
                .status(CollectionStatus.RESERVED)
                .timeReserved(LocalDateTime.now().minusHours(5))
                .build();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findAllByUserIdAndStatus(testUserId, CollectionStatus.RESERVED))
                .thenReturn(List.of(expiredReservation));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of());

        List<ReservationDTO> result = reservationService.getReservations("Bearer userAccessToken123", CollectionStatus.RESERVED);

        //expired reservation should not be in the returned list
        assertThat(result).isEmpty();

        //expired reservation should have been saved with NO_SHOW status
        verify(reservationRepository).saveAll(anyList());
        assertThat(expiredReservation.getStatus()).isEqualTo(CollectionStatus.NO_SHOW);

    }


    /**
     * getReservations test 3
     * a non-user token tries to get reservations
     * AuthorizationException thrown
     *
     */
    @Test
    public void testGetReservations_NonUser() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> reservationService.getReservations("Bearer vendorAccessToken123", CollectionStatus.RESERVED))
                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository, never()).findAllByUserIdAndStatus(any(), any());
    }


    /**
     * getReservations test 4
     * user has no active reservations
     * returns empty list
     */
    @Test
    public void testGetReservations_NoReservations() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findAllByUserIdAndStatus(testUserId, CollectionStatus.RESERVED))
                .thenReturn(List.of());
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of());

        List<ReservationDTO> result = reservationService.getReservations("Bearer userAccessToken123", CollectionStatus.RESERVED);

        assertThat(result).isNotNull();

        assertThat(result).isEmpty();
    }


}
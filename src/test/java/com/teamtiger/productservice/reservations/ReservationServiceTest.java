package com.teamtiger.productservice.reservations;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.reservations.entities.ClaimCode;
import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.exceptions.BundleAlreadyReservedException;
import com.teamtiger.productservice.reservations.exceptions.ReservationNotFoundException;
import com.teamtiger.productservice.reservations.models.*;

import com.teamtiger.productservice.reservations.repositories.ClaimCodeRepository;
import com.teamtiger.productservice.reservations.repositories.ReservationRepository;
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
//import static org.mockito.ArgumentMatchers.eq;
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

        //make sure we never use the database if auth fails
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





    /**
     * deleteReservation test 1
     * success
     * deleteById is called
     */
    @Test
    public void testDeleteReservation_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(mockReservation));
        doNothing().when(reservationRepository).deleteById(testReservationId);


        reservationService.deleteReservation(testReservationId, "Bearer userAccessToken123");

        verify(reservationRepository).deleteById(testReservationId);
    }


    /**
     * deleteReservation test 2
     * reservation doesnt exist in the database
     * ReservationNotFoundException thrown
     */
    @Test
    public void testDeleteReservation_ReservationNotFound() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //reservation cant be found
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteReservation(testReservationId, "Bearer userAccessToken123"))
                .isInstanceOf(ReservationNotFoundException.class);

        //ensure never call delete if reservation doesn't exist
        verify(reservationRepository, never()).deleteById(any());
    }



    /**
     * deleteReservation test 3
     * a user tries to delete another user's reservation
     * AuthorizationException thrown
     */
    @Test
    public void testDeleteReservation_WrongUser() {

        //different user to the one who owns the reservation
        UUID differentUserId = UUID.randomUUID();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(differentUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //reservation belongs to testUserId not differentUserId
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(mockReservation));


        assertThatThrownBy(() -> reservationService.deleteReservation(testReservationId, "Bearer wrongUserToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository, never()).deleteById(any());


    }




    /**
     * getClaimCode test 1
     * success
     * returns ClaimCodeDTO with the correct claim code
     *
     */
    @Test
    public void testGetClaimCode_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(mockReservation));

        //claim code already exists in the database
        when(claimCodeRepository.findById(testReservationId)).thenReturn(Optional.of(mockClaimCode));

        ClaimCodeDTO result = reservationService.getClaimCode(testReservationId, "Bearer userAccessToken123");

        assertThat(result).isNotNull();
        assertThat(result.getClaimCode()).isEqualTo("ABC123");
    }


    /**
     * getClaimCode test 2
     * no claim code exists yet — a new one is generated and saved
     */
    @Test
    public void testGetClaimCode_GeneratesNewCode() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(mockReservation));

        //no existing claim code found
        when(claimCodeRepository.findById(testReservationId)).thenReturn(Optional.empty());
        when(claimCodeGenerator.generateCode()).thenReturn("XYZ999");

        when(claimCodeRepository.save(any(ClaimCode.class))).thenReturn(mockClaimCode);

        ClaimCodeDTO result = reservationService.getClaimCode(testReservationId, "Bearer userAccessToken123");

        assertThat(result).isNotNull();

        //confirm a new code was generated and saved
        verify(claimCodeGenerator).generateCode();
        verify(claimCodeRepository).save(any(ClaimCode.class));


    }



    /**
     * getClaimCode test 3
     * a user tries to get the claim code for another user's reservation
     * AuthorizationException thrown
     */
    @Test
    public void testGetClaimCode_WrongUser() {

        UUID differentUserId = UUID.randomUUID();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(differentUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //reservation belongs to testUserId, not differentUserId
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(mockReservation));

        assertThatThrownBy(() -> reservationService.getClaimCode(testReservationId, "Bearer wrongUserToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(claimCodeRepository, never()).findById(any());
    }


    /**
     * getClaimCode test 4
     * reservation does not exist
     * ReservationNotFoundException thrown
     */

    @Test
    public void testGetClaimCode_ReservationNotFound() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getClaimCode(testReservationId, "Bearer userAccessToken123"))
                .isInstanceOf(ReservationNotFoundException.class);
    }














    /**
     * getClaimCode test 5
     * a non-user token tries to get a claim code
     * AuthorizationException thrown
     */
    @Test
    public void testGetClaimCode_NonUser() {

//        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
//        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> reservationService.getClaimCode(testReservationId, "Bearer vendorAccessToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository, never()).findById(any());
    }

//
//    /**
//     * checkClaimCode test 1
//     * success
//     * reservation is marked COLLECTED, claim code is deleted, event is published
//     */
//    @Test
//    public void testCheckClaimCode_Success() {
//
//        ClaimCodeDTO claimCodeDTO = ClaimCodeDTO.builder().claimCode("ABC123").build();
//
//        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
//        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
//        when(claimCodeRepository.findByClaimCodeAndReservation_Bundle_VendorId("ABC123", testVendorId))
//                .thenReturn(Optional.of(mockClaimCode));
//        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);
//        doNothing().when(claimCodeRepository).delete(mockClaimCode);
//        doNothing().when(reservationEventPublisher).publishReservationCollected(any(ReservationCollectedEvent.class));
//
//        ReservationVendorDTO result = reservationService.checkClaimCode(claimCodeDTO, "Bearer vendorAccessToken123");
//
//        assertThat(result).isNotNull();
//        assertThat(result.getReservationId()).isEqualTo(testReservationId);
//        assertThat(result.getBundleId()).isEqualTo(testBundleId);
//
//        //confirm reservation is marked collected and claim code deleted
//        assertThat(mockReservation.getStatus()).isEqualTo(CollectionStatus.COLLECTED);
//        verify(claimCodeRepository).delete(mockClaimCode);
//        verify(reservationEventPublisher).publishReservationCollected(any(ReservationCollectedEvent.class));
//    }


    /**
     * checkClaimCode test 2
     * claim code does not match any reservation for this vendor
     * BundleNotFoundException thrown
     */
    @Test
    public void testCheckClaimCode_InvalidCode() {

        ClaimCodeDTO claimCodeDTO = ClaimCodeDTO.builder().claimCode("BADCODE").build();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        //no match found for this claim code + vendor combination
        when(claimCodeRepository.findByClaimCodeAndReservation_Bundle_VendorId("BADCODE", testVendorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.checkClaimCode(claimCodeDTO, "Bearer vendorAccessToken123"))
                .isInstanceOf(BundleNotFoundException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    /**
     * checkClaimCode test 3
     * a non-vendor token tries to verify a claim code
     * AuthorizationException thrown
     */
    @Test
    public void testCheckClaimCode_NonVendor() {

        ClaimCodeDTO claimCodeDTO = ClaimCodeDTO.builder().claimCode("ABC123").build();


        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() -> reservationService.checkClaimCode(claimCodeDTO, "Bearer userAccessToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(claimCodeRepository, never()).findByClaimCodeAndReservation_Bundle_VendorId(any(), any());
    }


    /**
     * getReservationsForVendor test 1
     * success
     * returns a list of ReservationVendorDTOs for the authenticated vendor
     */
    @Test
    public void testGetReservationsForVendor_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(reservationRepository.findAllByStatusAndBundleVendorId(CollectionStatus.RESERVED, testVendorId))
                .thenReturn(List.of(mockReservation));

        List<ReservationVendorDTO> result = reservationService.getReservationsForVendor("Bearer vendorAccessToken123");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        //ensure the returned DTO has the correct bundle and reservation ids
        assertThat(result.get(0).getReservationId()).isEqualTo(testReservationId);
        assertThat(result.get(0).getBundleId()).isEqualTo(testBundleId);
        assertThat(result.get(0).getBundleName()).isEqualTo("Sweet Treat Bundle");
        assertThat(result.get(0).getAmountDue()).isEqualTo(4.71);

        verify(reservationRepository).findAllByStatusAndBundleVendorId(CollectionStatus.RESERVED, testVendorId);
    }





















    /**
     * getReservationsForVendor test 2
     * a non-vendor token tries to get vendor reservations
     * AuthorizationException thrown
     */
    @Test
    public void testGetReservationsForVendor_NonVendor() {

//        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testUserId);
//        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");


        assertThatThrownBy(() -> reservationService.getReservationsForVendor("Bearer userAccessToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository, never()).findAllByStatusAndBundleVendorId(any(), any());


    }


    /**
     * getReservationsForVendor test 3
     * vendor has no active reservations
     * returns empty list
     */
    @Test
    public void testGetReservationsForVendor_NoReservations() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(reservationRepository.findAllByStatusAndBundleVendorId(CollectionStatus.RESERVED, testVendorId))
                .thenReturn(List.of());

        List<ReservationVendorDTO> result = reservationService.getReservationsForVendor("Bearer vendorAccessToken123");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    /**
     * loadSeededData test 1
     * success
     * no exception thrown and saveAll is called
     */
    @Test
    public void testLoadSeededData_Success() {

        ReservationSeedDTO seedDTO = new ReservationSeedDTO(
                testReservationId,
                testBundleId,
                testUserId,
                CollectionStatus.COLLECTED,
                LocalDateTime.now().minusHours(5),
                LocalDateTime.now().minusHours(2)
        );

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("INTERNAL");
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.of(mockBundle));
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of(mockReservation));

        //no exception should be thrown
        reservationService.loadSeededData("Bearer internalToken123", List.of(seedDTO));

        verify(reservationRepository).saveAll(anyList());
    }


    /**
     * loadSeededData test 2
     * a non-internal token tries to load seeded data
     * AuthorizationException thrown
     */
    @Test
    public void testLoadSeededData_NonInternalRole() {

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> reservationService.loadSeededData("Bearer vendorToken123", List.of()))

                .isInstanceOf(AuthorizationException.class);

        verify(reservationRepository, never()).saveAll(anyList());
    }


    /**
     * loadSeededData test 3
     * bundle referenced in seed data does not exist
     * BundleNotFoundException thrown
     */
    @Test
    public void testLoadSeededData_BundleNotFound() {

        ReservationSeedDTO seedDTO = new ReservationSeedDTO(
                testReservationId,
                testBundleId,
                testUserId,
                CollectionStatus.RESERVED,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now()
        );

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("INTERNAL");

        //bundle referenced in seed data cannot be found
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.loadSeededData("Bearer internalToken123", List.of(seedDTO)))
                .isInstanceOf(BundleNotFoundException.class);

        verify(reservationRepository, never()).saveAll(anyList());
    }



}
package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceJPA implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BundleRepository bundleRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ClaimCodeGenerator claimCodeGenerator;
    private final ClaimCodeRepository claimCodeRepository;
    private final ReservationEventPublisher reservationEventPublisher;

    @Override
    public ReservationDTO createReservation(UUID bundleId, String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }

        if (reservationRepository.existsByBundleId(bundleId)) {
            throw new BundleAlreadyReservedException();
        }

        //Get the bundle reference for reservation
        Bundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(BundleNotFoundException::new);

        Reservation reservation = Reservation.builder()
                .bundle(bundle)
                .status(CollectionStatus.RESERVED)
                .userId(userId)
                .amountDue(bundle.getPrice())
                .timeReserved(LocalDateTime.now())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        //Generate and Save Claim Code
        String claimCode = claimCodeGenerator.generateCode();

        ClaimCode claimCodeEntity = ClaimCode.builder()
                .reservation(savedReservation)
                .claimCode(claimCode)
                .build();

        claimCodeRepository.save(claimCodeEntity);

        return ReservationMapper.toDTO(savedReservation);
    }


    @Override
    public List<ReservationDTO> getReservations(String accessToken, CollectionStatus status) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }


        List<Reservation> reservations = reservationRepository.findAllByUserIdAndStatus(userId, status);

        //Update Reservations that Have Expired
        List<Reservation> expiredReservations = reservations.stream()
                .filter(entity -> entity.getBundle().getCollectionEnd().isBefore(LocalDateTime.now()))
                .peek(entity -> entity.setStatus(CollectionStatus.NO_SHOW))
                .toList();

        List<Reservation> pendingReservations = reservations.stream()
                .filter(entity -> entity.getBundle().getCollectionEnd().isAfter(LocalDateTime.now()))
                .toList();

        reservationRepository.saveAll(expiredReservations);


        return pendingReservations.stream()
                .map(ReservationMapper::toDTO)
                .toList();
    }


    @Override
    public void deleteReservation(UUID reservationId, String accessToken) {

        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if (!role.equals("USER")){
            throw new AuthorizationException();
        }

        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.getUserId().equals(userId)) {
            throw new AuthorizationException();
        }

        reservationRepository.deleteById(reservationId);
    }

    @Override
    public ClaimCodeDTO getClaimCode(UUID reservationId, String accessToken) {

        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if (!role.equals("USER")){
            throw new AuthorizationException();
        }

        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservation.getUserId().equals(userId)) {
            throw new AuthorizationException();
        }


        //Generates a new claim code if one hasn't been made
        ClaimCode claimCode = claimCodeRepository.findById(reservation.getId()).orElseGet(() -> {
            String newClaimCode = claimCodeGenerator.generateCode();
            return claimCodeRepository.save(ClaimCode.builder()
                    .reservation(reservation)
                    .claimCode(newClaimCode)
                    .build());
        });

        return ClaimCodeDTO.builder()
                .claimCode(claimCode.getClaimCode())
                .build();
    }

    @Override
    public ReservationVendorDTO checkClaimCode(ClaimCodeDTO claimCode, String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        ClaimCode savedClaimCode = claimCodeRepository.findByClaimCodeAndReservation_Bundle_VendorId(claimCode.getClaimCode(), vendorId)
                .orElseThrow(BundleNotFoundException::new);

        Reservation reservation = savedClaimCode.getReservation();
        Bundle bundle = reservation.getBundle();

        ReservationVendorDTO reservationVendorDTO = ReservationVendorDTO.builder()
                .reservationId(reservation.getId())
                .bundleId(bundle.getId())
                .bundleName(bundle.getName())
                .amountDue(reservation.getAmountDue())
                .collectionStart(bundle.getCollectionStart())
                .collectionEnd(bundle.getCollectionEnd())
                .build();


        //Mark as collected and delete claim code
        reservation.setTimeCollected(LocalDateTime.now());
        reservation.setStatus(CollectionStatus.COLLECTED);
        reservationRepository.save(reservation);

        claimCodeRepository.delete(savedClaimCode);

        reservationEventPublisher.publishReservationCollected(new ReservationCollectedEvent(reservation.getUserId(), reservation.getTimeCollected()));

        return reservationVendorDTO;
    }

    @Override
    public void loadSeededData(String accessToken, List<ReservationSeedDTO> reservations) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        List<Reservation> entities = reservations.stream()
                .map(dto -> {

                    Bundle bundle = bundleRepository.findById(dto.getBundleId())
                            .orElseThrow(BundleNotFoundException::new);

                    return Reservation.builder()
                            .id(dto.getReservationId())
                            .bundle(bundle)
                            .status(dto.getStatus())
                            .userId(dto.getUserId())
                            .amountDue(bundle.getPrice())
                            .timeReserved(dto.getTimeReserved())
                            .timeCollected(dto.getTimeCollected())
                            .build();


                })
                .toList();

        reservationRepository.saveAll(entities);
    }

    @Override
    public List<ReservationVendorDTO> getReservationsForVendor(String accessToken) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        List<Reservation> currentReservations = reservationRepository.findAllByStatusAndBundleVendorId(CollectionStatus.RESERVED, vendorId);

        return currentReservations.stream()
                .map(entity -> ReservationVendorDTO.builder()
                        .bundleName(entity.getBundle().getName())
                        .reservationId(entity.getId())
                        .bundleId(entity.getBundle().getId())
                        .collectionStart(entity.getBundle().getCollectionStart())
                        .collectionEnd(entity.getBundle().getCollectionEnd())
                        .amountDue(entity.getAmountDue())
                        .build())
                .toList();
    }

    private static class ReservationMapper {

        public static ReservationDTO toDTO(Reservation reservation) {
            return ReservationDTO.builder()
                    .reservationId(reservation.getId())
                    .bundle(BundleMapper.toDTO(reservation.getBundle()))
                    .build();
        }

    }

    private static class BundleMapper {

        public static BundleDTO toDTO(Bundle entity) {
            return BundleDTO.builder()
                    .name(entity.getName())
                    .bundleId(entity.getId())
                    .description(entity.getDescription())
                    .price(entity.getPrice())
                    .retailPrice(entity.getRetailPrice())
                    .vendorId(entity.getVendorId())
                    .category(entity.getCategory())
                    .collectionStart(entity.getCollectionStart())
                    .collectionEnd(entity.getCollectionEnd())
                    .build();
        }
    }

}

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
import com.teamtiger.productservice.reservations.models.ClaimCodeDTO;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import com.teamtiger.productservice.reservations.models.ReservationDTO;
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

    @Override
    public ReservationDTO createReservation(UUID bundleId, String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        if(reservationRepository.existsByBundleId(bundleId)) {
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
    public List<ReservationDTO> getReservations(String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        List<Reservation> reservations = reservationRepository.findAllByUserIdAndStatus(userId, CollectionStatus.RESERVED);

        return reservations.stream()
                .map(ReservationMapper::toDTO)
                .toList();
    }


    @Override
    public void deleteReservation(UUID reservationId, String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if(!reservation.getUserId().equals(userId)) {
            throw new AuthorizationException();
        }

        reservationRepository.deleteById(reservationId);
    }

    @Override
    public ClaimCodeDTO getClaimCode(UUID reservationId, String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if(!reservation.getUserId().equals(userId)) {
            throw new AuthorizationException();
        }


        //Generates a new claim code if one hasn't been made
        ClaimCode claimCode = claimCodeRepository.findById(reservation).orElseGet(() -> {
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
    public void checkClaimCode(ClaimCodeDTO claimCode, String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        ClaimCode savedClaimCode = claimCodeRepository.findByClaimCodeAndReservation_Bundle_VendorId(claimCode.getClaimCode(), vendorId)
                .orElseThrow(BundleNotFoundException::new);

        //Mark as collected and delete claim code
        Reservation reservation = savedClaimCode.getReservation();
        reservation.setTimeCollected(LocalDateTime.now());
        reservation.setStatus(CollectionStatus.COLLECTED);
        reservationRepository.save(reservation);

        claimCodeRepository.delete(savedClaimCode);

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

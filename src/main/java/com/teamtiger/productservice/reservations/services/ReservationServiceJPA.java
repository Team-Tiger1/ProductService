package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.exceptions.BundleAlreadyReservedException;
import com.teamtiger.productservice.reservations.exceptions.ReservationNotFoundException;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import com.teamtiger.productservice.reservations.models.ReservationDTO;
import com.teamtiger.productservice.reservations.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceJPA implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BundleRepository bundleRepository;
    private final JwtTokenUtil jwtTokenUtil;

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

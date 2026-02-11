package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.reservations.models.*;

import java.util.List;
import java.util.UUID;

public interface ReservationService {

    ReservationDTO createReservation(UUID bundleId, String accessToken);

    List<ReservationDTO> getReservations(String accessToken);

    void deleteReservation(UUID reservationId, String accessToken);

    ClaimCodeDTO getClaimCode(UUID reservationId, String accessToken);

    void checkClaimCode(ClaimCodeDTO claimCode, String accessToken);

    void loadSeededData(String accessToken, List<ReservationSeedDTO> reservations);

    List<ReservationVendorDTO> getReservationsForVendor(String accessToken, CollectionStatus status);

}

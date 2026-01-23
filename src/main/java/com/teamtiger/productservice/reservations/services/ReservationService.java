package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.reservations.models.ReservationDTO;

import java.util.List;
import java.util.UUID;

public interface ReservationService {

    ReservationDTO createReservation(UUID bundleId, String accessToken);

    List<ReservationDTO> getReservations(String accessToken);

    void deleteReservation(UUID reservationId, String accessToken);

}

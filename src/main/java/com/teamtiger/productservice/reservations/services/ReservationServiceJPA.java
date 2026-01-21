package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.reservations.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceJPA implements ReservationService {

    private ReservationRepository reservationRepository;

}

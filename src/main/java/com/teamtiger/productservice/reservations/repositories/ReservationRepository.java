package com.teamtiger.productservice.reservations.repositories;

import com.teamtiger.productservice.reservations.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}

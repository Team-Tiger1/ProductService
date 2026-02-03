package com.teamtiger.productservice.reservations.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByBundleId(UUID bundleId);

    List<Reservation> findAllByUserIdAndStatus(UUID userId, CollectionStatus status);

}

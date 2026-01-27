package com.teamtiger.productservice.reservations.repositories;

import com.teamtiger.productservice.reservations.entities.ClaimCode;
import com.teamtiger.productservice.reservations.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClaimCodeRepository extends JpaRepository<ClaimCode, Reservation> {

    boolean existsByClaimCode(String claimCode);

    Optional<ClaimCode> findByClaimCodeAndReservation_Bundle_VendorId(String claimCode, UUID vendorId);

}

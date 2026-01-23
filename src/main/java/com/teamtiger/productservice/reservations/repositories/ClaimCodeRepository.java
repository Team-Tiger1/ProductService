package com.teamtiger.productservice.reservations.repositories;

import com.teamtiger.productservice.reservations.entities.ClaimCode;
import com.teamtiger.productservice.reservations.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimCodeRepository extends JpaRepository<ClaimCode, Reservation> {

    boolean existsByClaimCode(String claimCode);

}

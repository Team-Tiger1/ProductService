package com.teamtiger.productservice.reservations.repositories;

import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for database operations for the Reservation entity
 */
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByBundleId(UUID bundleId);

    List<Reservation> findAllByUserIdAndStatus(UUID userId, CollectionStatus status);

    List<Reservation> findAllByStatusAndBundleVendorId(CollectionStatus status, UUID vendorId);

    List<Reservation> findAllByUserId(UUID userId);

    Optional<Reservation> findByBundleId(UUID bundleId);

    @Query(value = "SELECT r.reservation_id, v.name, v.street_address, v.postcode " +
            "FROM vendor AS v " +
            "JOIN bundles b ON b.vendor_id = v.vendor_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.reservation_id IN :reservationIds " +
            "AND r.user_id = :userId", nativeQuery = true)
    List<Object[]> getAllVendorInfo(List<UUID> reservationIds, UUID userId);

}

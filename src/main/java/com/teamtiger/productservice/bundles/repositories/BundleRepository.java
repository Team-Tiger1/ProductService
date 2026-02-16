package com.teamtiger.productservice.bundles.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BundleRepository extends JpaRepository<Bundle, UUID> {

    List<Bundle> findAllByVendorId(UUID vendorId);

    @Query("SELECT b FROM Bundle b WHERE b.collectionEnd > CURRENT_TIMESTAMP AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b)")
    List<Bundle> findAvailableBundles(Pageable pageable);

    @Query("SELECT b FROM Bundle b WHERE b.vendorId = :vendorId AND b.collectionEnd > CURRENT_TIMESTAMP AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b ) ORDER BY b.price ASC")
    List<Bundle> findAvailableBundlesByVendor(UUID vendorId);


    @Query("SELECT COUNT(b.id) FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND b.postingTime BETWEEN :period AND CURRENT_TIMESTAMP AND r.status = 'COLLECTED' " +
            "GROUP BY r.status")
    Long countCollectedBundlesByVendorId(UUID vendorId, LocalDateTime period);

    @Query("SELECT COUNT(b.id) FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND b.collectionEnd BETWEEN :period AND CURRENT_TIMESTAMP AND r.timeCollected IS NULL AND b.collectionEnd < CURRENT_TIMESTAMP " +
            "GROUP BY r.status")
    Long countNoShowBundlesByVendorId(UUID vendorId, LocalDateTime period);

    @Query("SELECT COUNT(b.id) FROM Bundle b WHERE b.vendorId = :vendorId " +
            "AND b.postingTime BETWEEN :period AND CURRENT_TIMESTAMP  AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b )")
    Long countPreviousExpiredBundlesByVendor(UUID vendorId, LocalDateTime period);


    @Query("SELECT COUNT(b.id) FROM Bundle b WHERE b.vendorId = :vendorId AND b.collectionEnd > CURRENT_TIMESTAMP")
    Long countPostedBundlesByVendor(UUID vendorId);

    @Query("SELECT b FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND r.status = 'COLLECTED' AND r.timeCollected BETWEEN :period AND CURRENT_TIMESTAMP " +
            "ORDER BY r.status, b.postingTime DESC")
    List<Bundle> findPastCollectedBundlesByVendor(UUID vendorId, LocalDateTime period);

    @Query("SELECT b FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND b.collectionEnd BETWEEN :period AND CURRENT_TIMESTAMP AND r.timeCollected IS NULL AND b.collectionEnd < CURRENT_TIMESTAMP " +
            "ORDER BY r.status, b.postingTime DESC")
    List<Bundle> findPastNoShowBundlesByVendor(UUID vendorId, LocalDateTime period);

    @Query("SELECT b FROM Bundle b WHERE b.vendorId = :vendorId " +
            "AND b.collectionEnd BETWEEN :period AND CURRENT_TIMESTAMP  AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b ) ORDER BY b.postingTime DESC")
    List<Bundle> findExpiredBundlesByVendor(UUID vendorId, LocalDateTime period);


}

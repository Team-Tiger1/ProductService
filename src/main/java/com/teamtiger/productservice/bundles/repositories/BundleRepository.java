package com.teamtiger.productservice.bundles.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for database operations for the Bundle entity
 */
public interface BundleRepository extends JpaRepository<Bundle, UUID> {

    List<Bundle> findAllByVendorId(UUID vendorId);

    @Query("SELECT b FROM Bundle b WHERE b.collectionEnd > CURRENT_TIMESTAMP AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b)")
    List<Bundle> findAvailableBundles(Pageable pageable);

    @Query("SELECT b FROM Bundle b WHERE b.vendorId = :vendorId AND b.collectionEnd > CURRENT_TIMESTAMP AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b ) ORDER BY b.price ASC")
    List<Bundle> findAvailableBundlesByVendor(UUID vendorId);


    @Query("SELECT r.status, COUNT(b.id) FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND b.postingTime BETWEEN :period AND CURRENT_TIMESTAMP " +
            "GROUP BY r.status")
    List<Object[]> countBundlesByVendorId(UUID vendorId, LocalDateTime period);

    @Query("SELECT COUNT(b.id) FROM Bundle b WHERE b.vendorId = :vendorId " +
            "AND b.postingTime BETWEEN :period AND CURRENT_TIMESTAMP  AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b )")
    Long countPreviousExpiredBundlesByVendor(UUID vendorId, LocalDateTime period);


    @Query("SELECT COUNT(b.id) FROM Bundle b WHERE b.vendorId = :vendorId AND b.collectionEnd > CURRENT_TIMESTAMP")
    Long countPostedBundlesByVendor(UUID vendorId);

    @Query("SELECT r.status, b FROM Reservation AS r " +
            "JOIN r.bundle b " +
            "WHERE b.vendorId = :vendorId AND b.collectionEnd BETWEEN :period AND CURRENT_TIMESTAMP " +
            "ORDER BY r.status, b.postingTime DESC")
    List<Object[]> findPastBundlesByVendor(UUID vendorId, LocalDateTime period);

    @Query("SELECT b FROM Bundle b WHERE b.vendorId = :vendorId " +
            "AND b.collectionEnd BETWEEN :period AND CURRENT_TIMESTAMP  AND NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b ) ORDER BY b.postingTime DESC")
    List<Bundle> findExpiredBundlesByVendor(UUID vendorId, LocalDateTime period);


    @Query(value = "SELECT b.bundle_id, SUM(p.weight * bp.quantity) FROM bundles AS b " +
            "JOIN bundle_products bp ON bp.bundle_id = b.bundle_id " +
            "JOIN products p ON p.product_id = bp.product_id " +
            "WHERE b.bundle_id IN :bundleIds " +
            "GROUP BY b.bundle_id ", nativeQuery = true)
    List<Object[]> getWeightForAllBundles(List<UUID> bundleIds);

    @Query("SELECT r.bundle.id FROM Reservation r " +
            "JOIN Bundle b ON b = r.bundle " +
            "WHERE b.vendorId = :vendorId AND r.status = 'COLLECTED'")
    Set<UUID> findReservedBundleIdsByVendorId(UUID vendorId);


    @Query(value = "SELECT vendor_id, postcode FROM vendor", nativeQuery = true)
    List<Object[]> findAllVendorInfo();



}

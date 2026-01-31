package com.teamtiger.productservice.bundles.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BundleRepository extends JpaRepository<Bundle, UUID> {

    List<Bundle> findAllByVendorId(UUID vendorId);

    @Query("SELECT b FROM Bundle b WHERE NOT EXISTS " +
            "(SELECT r FROM Reservation r WHERE r.bundle = b)")
    List<Bundle> findAvailableBundles(Pageable pageable);

}

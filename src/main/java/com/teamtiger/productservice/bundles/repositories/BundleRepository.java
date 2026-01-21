package com.teamtiger.productservice.bundles.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BundleRepository extends JpaRepository<Bundle, UUID> {

    List<Bundle> findAllByVendorId(UUID vendorId);

}

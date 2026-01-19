package com.teamtiger.productservice.bundles.repositories;

import com.teamtiger.productservice.bundles.entities.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BundleRepository extends JpaRepository<Bundle, UUID> {
}

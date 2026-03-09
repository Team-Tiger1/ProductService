package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.entities.AllergyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Returned when listing bundles
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortBundleDTO {

    private UUID bundleId;
    private UUID vendorId;
    private String bundleName;
    private double price;
    private BundleCategory category;
    private Set<AllergyType> allergens;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;

}

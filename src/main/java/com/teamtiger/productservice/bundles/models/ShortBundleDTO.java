package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.entities.AllergyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
//Returned when listing bundles
public class ShortBundleDTO {

    private UUID bundleId;
    private String bundleName;
    private double price;
    private BundleCategory category;
    private Set<AllergyType> allergens;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;

}

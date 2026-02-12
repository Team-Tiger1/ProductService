package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.entities.AllergyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class BundleDTO {

    private String name;
    private UUID bundleId;
    private String description;
    private List<BundleProductDTO> productList;
    private UUID vendorId;
    private double retailPrice;
    private double price;
    private BundleCategory category;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private Set<AllergyType> allergens;

}

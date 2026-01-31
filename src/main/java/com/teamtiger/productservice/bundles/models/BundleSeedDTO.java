package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.entities.AllergyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BundleSeedDTO {

    @NotNull
    private UUID bundleId;

    @NotNull
    private Set<UUID> productIds;

    @NotNull
    private Set<AllergyType> allergyTypes;

    @NotNull
    private UUID vendorId;

    @NotBlank
    private String name;

    @NotNull
    private BundleCategory category;

    @NotNull
    private LocalDateTime collectionStart;

    @NotNull
    private LocalDateTime collectionEnd;

    @NotNull
    private LocalDateTime postingTime;

    private String description;

    @NotNull
    private double price;

}

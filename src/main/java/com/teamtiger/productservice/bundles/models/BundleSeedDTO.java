package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.entities.AllergyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Used for loading bundles from seeded data
 */
@Getter
@AllArgsConstructor
@Schema(description = "Request body for seeding bundle data")
public class BundleSeedDTO {

    @NotNull
    @Schema(description = "Bundle ID")
    private UUID bundleId;

    @NotNull
    @Schema(description = "List of product IDs")
    private List<UUID> productIds;

    @NotNull
    @Schema(
            description = "Bundles allergy types",
            implementation = AllergyType.class
    )
    private Set<AllergyType> allergyTypes;

    @NotNull
    @Schema(description = "Vendor ID")
    private UUID vendorId;

    @NotBlank
    @Schema(description = "Name")
    private String name;

    @NotNull
    @Schema(
            description = "Bundle category",
            implementation = BundleCategory.class
    )
    private BundleCategory category;

    @NotNull
    @Schema(description = "Collection start time")
    private LocalDateTime collectionStart;

    @NotNull
    @Schema(description = "Collection end time")
    private LocalDateTime collectionEnd;

    @NotNull
    @Schema(description = "Posting time")
    private LocalDateTime postingTime;

    @Schema(description = "Description")
    private String description;

    @NotNull
    @Schema(description = "Bundle description")
    private double price;

}

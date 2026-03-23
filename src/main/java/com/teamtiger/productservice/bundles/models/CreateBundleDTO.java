package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Returned to vendor when they create a Bundle
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a bundle")
public class CreateBundleDTO {

    @NotBlank
    @Schema(description = "Bundle name")
    private String name;
    @Schema(description = "Bundle description")
    private String description;

    @NotNull
    @NotEmpty
    @Schema(description = "List of product IDs")
    private List<UUID> productList;

    @NotNull
    @Schema(description = "Price")
    private double price;

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


}

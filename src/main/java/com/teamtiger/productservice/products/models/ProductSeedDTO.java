package com.teamtiger.productservice.products.models;

import com.teamtiger.productservice.products.entities.AllergyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;
/**
 * DTO used for bulk loading seeded data
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Request body for seeding product data")
public class ProductSeedDTO {

    @NotNull
    @Schema(description = "Product ID")
    private UUID productId;

    @NotNull
    @Schema(description = "Vendor ID")
    private UUID vendorId;

    @NotBlank
    @Schema(description = "Product name")
    private String name;

    @NotNull
    @Schema(description = "Retail price")
    private double retailPrice;

    @NotNull
    @Schema(description = "Product weight")
    private double weight;

    @NotNull
    @Schema(
            description = "Product allergy types",
            implementation = AllergyType.class
    )
    private Set<AllergyType> allergies;




}

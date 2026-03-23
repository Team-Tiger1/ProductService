package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * DTO used for updating product fields
 */
@Schema(description = "Request body for updating product fields")
public record UpdateProductDTO (
        @Schema(description = "Product name")
        String name,
        @Schema(description = "Retail price")
        Double retailPrice,
        @Schema(description = "Product weight")
        Double weight,
        @Schema(
                description = "Product allergy types",
                implementation = AllergyType.class
        )
        Set<AllergyType> allergies
)       {}

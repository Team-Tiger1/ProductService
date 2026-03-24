package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used by to create a Product
 */
@Schema(description = "Request body for creating a product")
public record ProductDTO(


    @NotBlank
    @Schema(description = "Product name")
    String name,

    @NotNull
    @Schema(description = "Retail price")
    Double retailPrice,

    @NotNull
    @Schema(description = "Product weight")
    Double weight,

    @NotNull
    @Schema(
            description = "Product allergy types",
            implementation = AllergyType.class
    )
    Set<AllergyType> allergies
) {
}

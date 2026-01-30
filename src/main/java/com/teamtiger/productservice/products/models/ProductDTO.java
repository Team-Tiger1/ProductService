package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ProductDTO(

    @NotBlank
    String name,

    @NotNull
    Double retailPrice,

    @NotNull
    Double weight,

    @NotNull
    Set<AllergyType> allergies
) {
}

package com.teamtiger.productservice.products.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ProductDTO(

    @NotBlank
    String name,

    @NotNull
    Double retailPrice,

    @NotNull
    Double weight
) {
}

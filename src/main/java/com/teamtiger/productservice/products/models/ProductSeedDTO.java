package com.teamtiger.productservice.products.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProductSeedDTO {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID vendorId;

    @NotBlank
    private String name;

    @NotNull
    private double retailPrice;

    @NotNull
    private double weight;




}

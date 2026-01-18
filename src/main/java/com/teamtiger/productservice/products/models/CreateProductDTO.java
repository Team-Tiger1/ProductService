package com.teamtiger.productservice.products.models;

import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;

public record CreateProductDTO(
    @NotNull String name,
    @NotNull BigDecimal retail_price,
    @NotNull int weight
) {
}

package com.teamtiger.productservice.products.models;

public record UpdateProductDTO (

        String name,
        Double retailPrice,
        Double weight
)    {}
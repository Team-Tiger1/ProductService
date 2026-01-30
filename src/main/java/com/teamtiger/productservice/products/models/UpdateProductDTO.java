package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;
import java.util.Set;
public record UpdateProductDTO (

    String name,
    Double retailPrice,
    Double weight,
    Set<AllergyType> allergies
)    {}

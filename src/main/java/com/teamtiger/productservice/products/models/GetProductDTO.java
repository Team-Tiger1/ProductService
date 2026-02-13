package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import java.util.UUID;

//DTO returned to client when Product data is fetched
public record GetProductDTO(
     UUID id,
     String name,
     Set<AllergyType> allergies,
     double retailPrice,
     double weight
)
{
}

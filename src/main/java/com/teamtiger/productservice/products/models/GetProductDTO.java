package com.teamtiger.productservice.products.models;
import com.teamtiger.productservice.products.entities.AllergyType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;


public record GetProductDTO(
     UUID id,
     String name,
     Double retailPrice,
     Double weight,
     Set<AllergyType>allergies
)
{
}

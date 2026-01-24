package com.teamtiger.productservice.products.models;
import java.math.BigDecimal;
import java.util.UUID;


public record GetProductDTO(
     UUID id,
     String name,
     double retailPrice,
     double weight
)
{
}

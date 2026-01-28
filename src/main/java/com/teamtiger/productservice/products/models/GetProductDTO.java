package com.teamtiger.productservice.products.models;
import java.util.UUID;


public record GetProductDTO(
     UUID id,
     String name,
     double retailPrice,
     double weight
)
{
}

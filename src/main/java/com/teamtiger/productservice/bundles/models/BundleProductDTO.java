package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.products.entities.AllergyType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
//Used to represent a product entry in a bundle response
public class BundleProductDTO {

    private UUID productId;
    private String productName;
    private double price;
    private int quantity;
    private Set<AllergyType> allergies;

}

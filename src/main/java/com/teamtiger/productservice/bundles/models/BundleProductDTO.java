package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.products.entities.AllergyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Used to represent a product entry in a bundle response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleProductDTO {

    private UUID productId;
    private String productName;
    private double price;
    private int quantity;
    private Set<AllergyType> allergens;

}

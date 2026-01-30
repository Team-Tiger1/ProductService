package com.teamtiger.productservice.products.mappers;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.GetProductDTO;

import java.util.Set;
import java.util.stream.Collectors;

public class ProductMapper {



    public static GetProductDTO toDTO(Product product) {
        Set<AllergyType> allergyTypes = product.getAllergies().stream()
                .map(Allergy::getAllergy)
                .collect(Collectors.toSet());
        return new GetProductDTO(
                product.getId(),
                product.getName(),
                product.getRetailPrice(),
                product.getWeight(),
                allergyTypes

        );

    }

}

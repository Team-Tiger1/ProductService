package com.teamtiger.productservice.products.mappers;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.GetProductDTO;

import java.util.Set;
import java.util.stream.Collectors;
//Used to convert the Product entity into its DTO representation for API responses
public class ProductMapper {



    public static GetProductDTO toDTO(Product product) {
        //Converts Allergy entities into corresponding enum type
        Set<AllergyType> allergyTypes = product.getAllergies().stream()
                .map(Allergy::getAllergyType)
                .collect(Collectors.toSet());
        return new GetProductDTO(
                product.getId(),
                product.getName(),
                allergyTypes,
                product.getRetailPrice(),
                product.getWeight()
                );

    }

}

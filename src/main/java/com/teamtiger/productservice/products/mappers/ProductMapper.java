package com.teamtiger.productservice.products.mappers;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.GetProductDTO;

public class ProductMapper {



    public static GetProductDTO toDTO(Product product) {
        return new GetProductDTO(
                product.getId(),
                product.getName(),
                product.getRetail_price(),
                product.getWeight()
        );

    }

}

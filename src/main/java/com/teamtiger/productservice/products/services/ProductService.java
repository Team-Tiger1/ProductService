package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.CreateProductDTO;

public interface ProductService {

    CreateProductDTO createProduct(String accessToken, CreateProductDTO dto);
}

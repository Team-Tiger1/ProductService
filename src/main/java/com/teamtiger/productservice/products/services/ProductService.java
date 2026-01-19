package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.models.ProductDTO;

public interface ProductService {

    ProductDTO createProduct(String accessToken, ProductDTO dto);
}

package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(String accessToken, ProductDTO dto);
    List<GetProductDTO> getVendorProducts(String accessToken);
}

package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.CreateProductDTO;
import com.teamtiger.productservice.products.models.GetProductDTO;

import java.util.List;

public interface ProductService {

    CreateProductDTO createProduct(String accessToken, CreateProductDTO dto);

    public List<GetProductDTO> getVendorProducts(String accessToken);
}

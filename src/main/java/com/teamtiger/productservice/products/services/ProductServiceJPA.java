package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.CreateProductDTO;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceJPA implements ProductService{

    private final ProductRepository productRepository;


    @Override
    public CreateProductDTO createProduct(String accessToken, CreateProductDTO dto) {


    }

}

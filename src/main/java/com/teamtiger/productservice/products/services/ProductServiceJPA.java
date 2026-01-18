package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.JwtTokenUtil;
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
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public CreateProductDTO createProduct(String accessToken, CreateProductDTO dto) {


        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product product = Product.builder()
                .name(dto.name())
                .retail_price(dto.retail_price())
                .weight(dto.weight())
                .vendor_id(vendorId)
                .build();

        Product createdProduct = productRepository.save(product);

        return dto;
    }

}

package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceJPA implements ProductService{

    private final ProductRepository productRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public ProductDTO createProduct(String accessToken, ProductDTO dto) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product product = Product.builder()
                .name(dto.name())
                .retailPrice(dto.retailPrice())
                .weight(dto.weight())
                .vendorId(vendorId)
                .build();

        Product createdProduct = productRepository.save(product);

        return dto;
    }

    @Override
    public List<GetProductDTO> getVendorProducts(String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        List<Product> productList = productRepository.findAllByVendorId(vendorId);

        return productList.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }



}

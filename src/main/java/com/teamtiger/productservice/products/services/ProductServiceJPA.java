package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    @Override
    public void deleteProduct(String accessToken, UUID productId) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product ProductToBeDeleted = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        productRepository.delete(ProductToBeDeleted);


    }

    @Override
    public GetProductDTO updateProduct(String accessToken, UUID productId, UpdateProductDTO dto) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product ProductToBeUpdated = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        if(!ProductToBeUpdated.getVendorId().equals(vendorId)){
            throw new RuntimeException("Not the vendor");
        }

        if(dto.name()!=null){
            ProductToBeUpdated.setName(dto.name());
        }

        if (dto.retailPrice() != null) {
            ProductToBeUpdated.setRetailPrice(dto.retailPrice());
        }

        if(dto.weight()!=null){
            ProductToBeUpdated.setWeight(dto.weight());
        }

        Product saved = productRepository.save(ProductToBeUpdated);
        return ProductMapper.toDTO(saved);

    }


}

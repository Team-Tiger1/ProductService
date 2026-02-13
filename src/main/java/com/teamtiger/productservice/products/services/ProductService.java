package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.ProductSeedDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;

import java.util.List;
import java.util.UUID;

//defines different operations for the management of products
public interface ProductService {

    GetProductDTO createProduct(String accessToken, ProductDTO dto);

    List<GetProductDTO> getVendorProducts(String accessToken);

    void deleteProduct(String accessToken, UUID productId);

    GetProductDTO updateProduct(String accessToken, UUID productId, UpdateProductDTO dto);

    void loadSeededData(String accessToken, List<ProductSeedDTO> products);
}

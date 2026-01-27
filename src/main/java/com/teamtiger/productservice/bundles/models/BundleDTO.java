package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.products.models.ProductDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BundleDTO {

    private String name;
    private String description;
    private List<ProductDTO> productList;
    private UUID vendorId;
    private double retailPrice;
    private double price;
    private BundleCategory category;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;

}

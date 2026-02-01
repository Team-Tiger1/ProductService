package com.teamtiger.productservice.bundles.entities;

import com.teamtiger.productservice.products.entities.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bundle_products")
public class BundleProduct {

    public BundleProduct(Bundle bundle, Product product, Integer quantity) {
        this.bundle = bundle;
        this.product = product;
        this.quantity = quantity;
        this.id = new BundleProductId(bundle.getId(), product.getId());
    }

    @EmbeddedId
    private BundleProductId id;

    @ManyToOne
    @MapsId("bundleId")
    private Bundle bundle;

    @ManyToOne
    @MapsId("productId")
    private Product product;

    private Integer quantity;



    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BundleProductId implements Serializable {
        private UUID bundleId;
        private UUID productId;
    }

}


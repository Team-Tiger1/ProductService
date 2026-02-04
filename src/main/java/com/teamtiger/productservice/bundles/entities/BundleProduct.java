package com.teamtiger.productservice.bundles.entities;

import com.teamtiger.productservice.products.entities.Product;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bundle_products")
public class BundleProduct {

    public BundleProduct(Bundle bundle, Product product, Integer quantity) {
        this.bundle = bundle;
        this.product = product;
        this.quantity = quantity;
    }

    @EmbeddedId
    private BundleProductId id;

    @ManyToOne
    @MapsId("bundleId")
    @JoinColumn(name = "bundle_id")
    private Bundle bundle;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleProduct that = (BundleProduct) o;

        return bundle.equals(that.bundle) && product.equals(that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundle, product);
    }

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BundleProductId implements Serializable {
        private UUID bundleId;
        private UUID productId;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BundleProductId that = (BundleProductId) o;
            return Objects.equals(bundleId, that.bundleId)
                    && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bundleId, productId);
        }

    }

}


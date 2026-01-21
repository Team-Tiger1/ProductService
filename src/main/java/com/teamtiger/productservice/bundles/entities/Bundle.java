package com.teamtiger.productservice.bundles.entities;

import com.teamtiger.productservice.products.entities.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(
        name = "bundles",
        indexes = {
                @Index(name = "idx_vendor_id", columnList = "vendor_id")
        }

)
public class Bundle {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "bundle_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToMany
    @JoinTable(
            name="bundle_products",
            joinColumns = @JoinColumn(name = "bundle_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products;

    @Column(name = "vendor_id", updatable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar") //REMOVE THIS WHEN MIGRATING TO POSTGRESQL
    private BundleCategory category;

    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private String description;
    private double retailPrice;
    private double price;

}

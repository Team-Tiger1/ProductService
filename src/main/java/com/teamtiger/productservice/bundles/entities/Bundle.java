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
@Table(name = "bundles")
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
    private Set<Product> productId;

    @Column(updatable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private BundleCategory category;

    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private String description;
    private double retailPrice;
    private double price;

}

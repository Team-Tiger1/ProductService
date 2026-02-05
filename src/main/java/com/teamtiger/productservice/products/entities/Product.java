package com.teamtiger.productservice.products.entities;

import com.teamtiger.productservice.bundles.entities.BundleProduct;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
//    @GeneratedValue(generator = "UUID")
//    @UuidGenerator
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID id;


    @ManyToMany
    @JoinTable(
            name = "product_allergy",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> allergies;

    private String name;
    private double retailPrice;
    private double weight;
    private UUID vendorId;

    @Version
    private long version;


}

package com.teamtiger.productservice.products.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
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
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID id;
    private String name;
    private BigDecimal retail_price;
    private int weight;
    private UUID vendor_id;

}

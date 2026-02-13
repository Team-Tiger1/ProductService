package com.teamtiger.productservice.bundles.entities;
//Entity for representing a Bundle
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "bundles"
)
public class Bundle {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "bundle_id", updatable = false, nullable = false)
    private UUID id;

    //Links products to this bundle
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<BundleProduct> bundleProducts = new HashSet<>();

    //Stores uniques allergies from products in this bundle
    @ManyToMany
    @JoinTable(
            name = "bundle_allergy",
            joinColumns = @JoinColumn(name = "bundle_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    @Builder.Default
    private Set<Allergy> allergies = new HashSet<>();

    @Column(name = "vendor_id", updatable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar")
    private BundleCategory category;

    private LocalDateTime postingTime;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private String description;
    private double retailPrice;
    private double price;


    //Adding a product includes both type of product and amount
    @Version
    private long version;
    public void addProduct(Product product, Integer quantity) {


        if(bundleProducts == null) {
            bundleProducts = new HashSet<>();
        }

        BundleProduct bundleProduct = new BundleProduct(this, product, quantity);
        bundleProducts.add(bundleProduct);

    }

}

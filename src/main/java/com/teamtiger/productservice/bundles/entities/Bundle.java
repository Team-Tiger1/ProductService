package com.teamtiger.productservice.bundles.entities;

import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(
        name = "bundles"
)
public class Bundle {

    @Id
//    @GeneratedValue(generator = "UUID")
//    @UuidGenerator
    @Column(name = "bundle_id", updatable = false, nullable = false)
    private UUID id;


    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BundleProduct> bundleProducts;


    @ManyToMany
    @JoinTable(
            name = "bundle_allergy",
            joinColumns = @JoinColumn(name = "bundle_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> allergies;

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



    @Version
    private long version;

    public void addProduct(Product product, Integer quantity) {

        if(product.getBundleProducts() == null) {
            product.setBundleProducts(new ArrayList<>());
        }

        if(bundleProducts == null) {
            bundleProducts = new ArrayList<>();
        }

        BundleProduct bundleProduct = new BundleProduct(this, product, quantity);
        bundleProducts.add(bundleProduct);
        product.getBundleProducts().add(bundleProduct);
    }

}

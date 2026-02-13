package com.teamtiger.productservice.products.entities;
//Entity for representing an allergen
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "allergens")
public class Allergy {

    @Id
    @Column(name = "allergy_id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AllergyType allergyType;




}

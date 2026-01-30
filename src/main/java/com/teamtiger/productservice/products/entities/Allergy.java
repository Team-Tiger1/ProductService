package com.teamtiger.productservice.products.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "allergies")
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AllergyType allergy;




}

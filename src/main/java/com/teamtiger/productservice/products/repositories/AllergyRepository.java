package com.teamtiger.productservice.products.repositories;

import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
//Repository for database operations for the Allergy entity
public interface AllergyRepository  extends JpaRepository<Allergy, Long> {
    Optional<Allergy> findByAllergyType(AllergyType allergy);

    Set<Allergy> findAllByAllergyTypeIn(Set<AllergyType> allergySet);

}

package com.teamtiger.productservice.products.repositories;

import com.teamtiger.productservice.products.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {



}

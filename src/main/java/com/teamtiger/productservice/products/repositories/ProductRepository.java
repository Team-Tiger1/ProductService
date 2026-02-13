package com.teamtiger.productservice.products.repositories;

import com.teamtiger.productservice.products.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
//Repository for database operations for the Product entity
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByVendorId(UUID vendorId);



}

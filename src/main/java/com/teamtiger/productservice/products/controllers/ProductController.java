package com.teamtiger.productservice.products.controllers;

import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.models.CreateProductDTO;
import com.teamtiger.productservice.products.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @PostMapping
    public ResponseEntity<?> createProduct(@RequestHeader("Authorization") String authHeader, @RequestBody CreateProductDTO dto) {

        try{
            String accessToken = authHeader.replace("Bearer ", "");
            CreateProductDTO createdProductDTO = productService.createProduct(accessToken,dto);
            return ResponseEntity.ok(createdProductDTO);



        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/vendor")
    public ResponseEntity<?> getVendorProducts(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            return ResponseEntity.ok(productService.getVendorProducts(accessToken));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching vendor products");
        }
    }






}

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
            //Returns a 500 error
            return ResponseEntity.internalServerError().build();
        }

        

    }



}

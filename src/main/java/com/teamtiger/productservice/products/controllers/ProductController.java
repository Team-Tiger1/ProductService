package com.teamtiger.productservice.products.controllers;

import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @Operation(summary = "Allows a Vendor to add a new Product")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestHeader("Authorization") String authHeader, @RequestBody ProductDTO dto) {

        try{
            String accessToken = authHeader.replace("Bearer ", "");
            ProductDTO createdProductDTO = productService.createProduct(accessToken,dto);
            return ResponseEntity.ok(createdProductDTO);



        }catch (Exception ex){
            //Returns a 500 error
            return ResponseEntity.internalServerError().build();
        }

        

    }



}

package com.teamtiger.productservice.products.controllers;


import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;
import com.teamtiger.productservice.products.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            GetProductDTO createdProductDTO = productService.createProduct(accessToken,dto);
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


    @Operation(summary = "Allows the vendor to delete a product")
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@RequestHeader("Authorization") String authHeader, @PathVariable UUID productId) {

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            productService.deleteProduct(accessToken, productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting product");
        }

    }

    @Operation(summary = "Allows the vendor to update fields for a product")
    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") String authHeader, @PathVariable UUID productId, @RequestBody UpdateProductDTO dto) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            return ResponseEntity.ok(productService.updateProduct(accessToken, productId, dto));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating product");
        }
    }


}

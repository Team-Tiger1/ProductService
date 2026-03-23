package com.teamtiger.productservice.products.controllers;


import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.ProductSeedDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;
import com.teamtiger.productservice.products.services.ProductService;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor


public class ProductController {

    private final ProductService productService;


    /**
     * Creates a product for the authenticated user
     * @param authHeader A bearer access token
     * @param dto contaning valid Product details
     * @return ResponseEntity that returns 200 if successful
     *        500 if an unexpected error occurs
     */
    @Operation(summary = "Allows a Vendor to add a new Product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetProductDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestHeader("Authorization") String authHeader, @RequestBody ProductDTO dto) {

        try{
            String accessToken = authHeader.replace("Bearer ", "");
            GetProductDTO createdProductDTO = productService.createProduct(accessToken,dto);
            return ResponseEntity.ok(createdProductDTO);
        }

        catch (Exception ex){
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * //Returns all products that authenticated Vendors owns
     * @param authHeader A bearer access token
     * @return  ResponseEntity that returns 200 if successful
     *          500 if an unexpected error occurs
     */
    @GetMapping("/vendor")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendor products returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GetProductDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @Operation(summary = "Returns all products belonging to the vendor")
    public ResponseEntity<?> getVendorProducts(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            return ResponseEntity.ok(productService.getVendorProducts(accessToken));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching vendor products");
        }
    }


    /** Deletes a product owned by the authenticated vendor
     *
     * @param authHeader  A bearer access token
     * @param productId of the product to be deleted
     * @return 204 to indicate successful deletion
     *         500 Exception returned if an error occurs
     */
    @Operation(summary = "Allows the vendor to delete a product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@RequestHeader("Authorization") String authHeader, @PathVariable UUID productId) {

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            productService.deleteProduct(accessToken, productId);
            return ResponseEntity.noContent().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting product");
        }

    }




    /**
     * Vendor updates fields for one of their product
     * @param authHeader A bearer access token
     * @param productId of the product to be patched/updated
     * @param dto containing fields of product to potentially be updated
     * @return  A ResponseEntity that returns 200 if successful
     *         500 Exception returned if an error occurs
     */
    @Operation(summary = "Allows the vendor to update fields for a product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetProductDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@RequestHeader("Authorization") String authHeader, @PathVariable UUID productId, @RequestBody UpdateProductDTO dto) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            return ResponseEntity.ok(productService.updateProduct(accessToken, productId, dto));
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating product");
        }
    }

    /**
     * Allows for bulk transfer of data
     * @param authHeader A bearer access token
     * @param products list containing seeded data
     * @return A ResponseEntity that returns 204 if successful
     *        500 Exception returned if an error occurs
     *        401 if unauthorized
     *        500 if a different error occurs
     */
    @Operation(summary = "Allows for bulk transfer of seeded data")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Seeded product data loaded successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping("/internal")
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody List<ProductSeedDTO> products) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            productService.loadSeededData(accessToken, products);
            return ResponseEntity.noContent().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}

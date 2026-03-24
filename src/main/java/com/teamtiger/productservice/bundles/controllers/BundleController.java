package com.teamtiger.productservice.bundles.controllers;

import com.teamtiger.productservice.bundles.exceptions.BundleCollectionTimeException;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.*;
import com.teamtiger.productservice.bundles.services.BundleService;
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

/**
 * Handles REST API requests for Bundles
 */
@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;


    /**
     * Allows an authenticated user to create a Bundle
     * @param createBundleDTO request body containing bundle details
     * @param authHeader Header containing JWT token
     * @return  A ResponseEntity containing the created BundleDTO that returns 200 if successful
     * 500 Exception returned if an error occurs
     */
    @Operation(summary = "Allows a Vendor to create a new Bundle")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bundle created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BundleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<?> createBundle(@Valid @RequestBody CreateBundleDTO createBundleDTO,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            BundleDTO bundleDTO = bundleService.createBundle(createBundleDTO, accessToken);
            return ResponseEntity.ok(bundleDTO);
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Allows an authenticated user to delete a bundle they own
     * @param authHeader containing JWT token
     * @param bundleId Unique ID of the bundle to be deleted
     * @return 204 if bundle is successfully deleted
     *          404 if the bundle to be deleted is not found
     *          401 if the vendor attempting to delete the bundle is not authorized to
     *          500 if unexpected error occurs
     */
    @Operation(summary = "Allows a Vendor to delete a bundle")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Bundle deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bundle not found",
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
    @DeleteMapping("/{bundleId}")
    public ResponseEntity<?> deleteBundle(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable UUID bundleId) {
        try {
            //Extracts raw JWT
            String accessToken = authHeader.replace("Bearer ", "");
            bundleService.deleteBundle(bundleId, accessToken);
            return ResponseEntity.noContent().build();
        }

        catch (BundleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (VendorAuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Returns all bundles belonging to the vendor attempting to access them
     * @param authHeader containing JWT token
     * @return  A ResponseEntity that returns 200 if successful
     *          401 error if bundles do not all belong to vendor
     *          500 if an unexpected error occurs
     */
    @Operation(summary = "Allows a vendor to access all their bundles for sale")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bundles retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BundleDTO.class))
                    )
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
    @GetMapping("/me")
    public ResponseEntity<?> getOwnBundles(@RequestHeader("Authorization") String authHeader) {
        try {
            //Extracts raw JWT
            String accessToken = authHeader.replace("Bearer ", "");
            List<BundleDTO> bundleList = bundleService.getOwnBundles(accessToken);
            return ResponseEntity.ok(bundleList);
        }

        catch (VendorAuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Returns List of all bundles from a specific vendor
     * @param vendorId A vendors unique identifier
     * @return A ResponseEntity that returns 200 if successful
     *
     */
    @Operation(summary = "Get all bundles from a vendor given a vendorId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendor bundles retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ShortBundleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorBundles(@PathVariable UUID vendorId) {
        try {
            List<ShortBundleDTO> bundleList = bundleService.getVendorBundles(vendorId);
            return ResponseEntity.ok(bundleList);
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     *
     * @param authHeader JWT header
     * @param bundles
     * @return A ResponseEntity that returns 204 if successful
     *        401 if unauthorized
     *        500 exception if a different error occurs
     */
    @Operation(summary = "Allows bulk transfer of seeded data")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Seeded bundle data loaded successfully",
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
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody List<BundleSeedDTO> bundles) {
        try {
            //Extracts raw JWT
            String accessToken = authHeader.replace("Bearer ", "");
            bundleService.loadSeededData(accessToken, bundles);
            return ResponseEntity.noContent().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Retrieves all available bundles
     * @param limit maximum amount of Bundles that can be returned
     * @param offset Page offset for pagination
     * @return A ResponseEntity that returns 200 if successful
     *         500 exception if an error occurs
     */
    @Operation(summary = "Get all available bundles")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Available bundles returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ShortBundleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllBundlesAvailable(@RequestParam(name = "limit", defaultValue = "50", required = false) int limit,
                                                    @RequestParam(name = "offset", defaultValue = "0", required = false) int offset) {
        try {
            List<ShortBundleDTO> bundleDTOS = bundleService.getAllBundles(limit, offset);
            return ResponseEntity.ok(bundleDTOS);
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Retrieves information about a specific bundle
     * @param bundleId unique identifier for a bundle
     * @param authHeader containing JWT token
     * @return ResponseEntity that returns 200 if successful
     *        401 if unauthorized
     *        404 error if bundle is not found
     *        500 if an unexpected error occurs
     */
    @Operation(summary = "Get detailed information about a bundle")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bundle returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BundleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bundle not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/detailed/{bundleId}")
    public ResponseEntity<?> getDetailedBundle(@PathVariable UUID bundleId, @RequestHeader("Authorization") String authHeader) {
        try {
            //Extracts raw JWT
            String accessToken = authHeader.replace("Bearer ", "");
            BundleDTO dto = bundleService.getDetailedBundle(accessToken, bundleId);
            return ResponseEntity.ok(dto);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (BundleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Processes a vendors request to get an outline of their bundle metrics
     * @param period DAY,WEEK,MONTH,YEAR
     * @param authHeader Authorization Header
     * @return Number of Collected, No Show and Expired Bundles
     */
    @Operation(summary = "Get number of bundles in a time period")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bundle metrics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BundleMetricDTO.class)
                    )
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
    @GetMapping("/metrics")
    public ResponseEntity<?> getBundleMetrics(@RequestParam(name = "period", defaultValue = "week", required = false) String period,
                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            //Extracts raw JWT
            String accessToken = authHeader.replace("Bearer ", "");
            BundleMetricDTO bundleMetricDTO = bundleService.getBundleMetrics(accessToken, period);
            return ResponseEntity.ok(bundleMetricDTO);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Retrieves bundle metrics for a past bundle for the authenticated vendor
     * @param period Time("day","week","month")
     * @param authHeader containing JWT token
     * @return BundleDTO containing information about the bundle
     *      401 if unauthorized
     *      500 if an unexpected error occurs
     */
    @Operation(summary = "Get bundle information in a time period")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Past bundles retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PastBundleDTO.class))
                    )
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
    @GetMapping("/analytics")
    public ResponseEntity<?> getPastBundles(@RequestParam(name = "period", defaultValue = "week", required = false) String period,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            List<PastBundleDTO> pastBundleDTO = bundleService.getPastBundles(accessToken, period);
            return ResponseEntity.ok(pastBundleDTO);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a vendors request to update a bundle
     * @param authHeader Authorization Header
     * @param bundleId Target Bundle for Update
     * @param editBundleDTO New Bundle Details
     * @return 204 No Content,
     * 404 if bundle not found
     * 401 if vendor is not owner of bundle
     * 400 if collection time is invalid
     * 500 if other error occurs
     */
    @Operation(summary = "Allows a vendor to update a bundle's details")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Bundle updated successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bundle not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Not a valid collection time",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PatchMapping("/{bundleId}")
    public ResponseEntity<?> editBundle(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable UUID bundleId,
                                        @RequestBody EditBundleDTO editBundleDTO) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            bundleService.updateBundle(accessToken, bundleId, editBundleDTO);
            return ResponseEntity.noContent().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (BundleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (BundleCollectionTimeException e) {
            return ResponseEntity.badRequest().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a vendors request to get collection rates of different discount rates
     * @param authHeader The authorization header
     * @return A list of discount rates
     */
    @Operation(summary = "Gets collection rates through different discount rates")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Discount rates retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DiscountDTO.class))
                    )
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
    @GetMapping("/analytics/discount")
    public ResponseEntity<?> getDiscountRates(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            List<DiscountDTO> discountDTOS = bundleService.getDiscountSellThroughRate(accessToken);
            return ResponseEntity.ok(discountDTOS);

        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

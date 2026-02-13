package com.teamtiger.productservice.bundles.controllers;

import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.*;
import com.teamtiger.productservice.bundles.services.BundleService;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
//Handles REST API requests for Bundles
public class BundleController {

    private final BundleService bundleService;


    //Allows a Vendor to create a new Bundle
    @Operation(summary = "Allows a Vendor to create a new Bundle")
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
    //Allows a Vendor to delete a bundle
    @Operation(summary = "Allows a Vendor to delete a bundle")
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
    //Allows a vendor to access all their bundles for sale
    @Operation(summary = "Allows a vendor to access all their bundles for sale")
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


    //Get all bundles from a vendor given a vendorId
    @Operation(summary = "Get all bundles from a vendor given a vendorId")
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

    //Allows bulk transfer of seeded data
    @Operation(summary = "Allows bulk transfer of seeded data")
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
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    //Get all available bundles
    @Operation(summary = "Get all available bundles")
    @GetMapping
    public ResponseEntity<?> getAllBundlesAvailable(@RequestParam(name = "limit", defaultValue = "50", required = false) int limit,
                                                    @RequestParam(name = "offset", defaultValue = "0", required = false) int offset) {
        try {
            List<ShortBundleDTO> bundleDTOS = bundleService.getAllBundles(limit, offset);
            return ResponseEntity.ok(bundleDTOS);
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //Get detailed information about a bundle
    @Operation(summary = "Get detailed information about a bundle")
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

//    @Operation(summary = "Get analytics all reservations for a vendor in a time period")
//    @GetMapping("/vendor")
//    public ResponseEntity<?> getPastReservations(@RequestParam(name = "period", defaultValue = "week", required = false)
//                                                 @RequestHeader("Authorization") String authHeader) {
//        try {
//
//            String accessToken = authHeader.replace("Bearer ", "");
//
//
//        }
//
//        catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    //Get number of bundles in a time period
    @Operation(summary = "Get number of bundles in a time period")
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
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }






}

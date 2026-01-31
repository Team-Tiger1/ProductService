package com.teamtiger.productservice.bundles.controllers;

import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.BundleSeedDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.models.ShortBundleDTO;
import com.teamtiger.productservice.bundles.services.BundleService;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

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

    @Operation(summary = "Allows a Vendor to delete a bundle")
    @DeleteMapping("/{bundleId}")
    public ResponseEntity<?> deleteBundle(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable UUID bundleId) {
        try {
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

    @Operation(summary = "Allows a vendor to access all their bundles for sale")
    @GetMapping("/me")
    public ResponseEntity<?> getOwnBundles(@RequestHeader("Authorization") String authHeader) {
        try {
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



    @Operation(summary = "Get all bundles from a vendor given a vendorId")
    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorBundles(@PathVariable UUID vendorId) {
        try {
            List<BundleDTO> bundleList = bundleService.getVendorBundles(vendorId);
            return ResponseEntity.ok(bundleList);
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Allows bulk transfer of seeded data")
    @PostMapping("/internal")
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody List<BundleSeedDTO> bundles) {
        try {
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

    @Operation(summary = "Get all available bundles")
    @GetMapping
    public ResponseEntity<?> getAllBundlesAvailable() {
        try {
            List<ShortBundleDTO> bundleDTOS = bundleService.getAllBundles();
            return ResponseEntity.ok(bundleDTOS);
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }






}

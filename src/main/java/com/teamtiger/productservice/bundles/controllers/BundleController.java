package com.teamtiger.productservice.bundles.controllers;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.services.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }





}

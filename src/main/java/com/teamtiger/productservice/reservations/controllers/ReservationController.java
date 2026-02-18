package com.teamtiger.productservice.reservations.controllers;

import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.exceptions.BundleAlreadyReservedException;
import com.teamtiger.productservice.reservations.exceptions.ReservationNotFoundException;
import com.teamtiger.productservice.reservations.models.*;
import com.teamtiger.productservice.reservations.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
//REST controller managing reservation operations
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * //Create a reservation given a bundle
     * @param bundleId of th bundle being Reserved
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 200 if successful
     *
     */
    @Operation(summary = "Create a reservation given a bundle")
    @PostMapping("/{bundleId}")
    public ResponseEntity<?> createReservation(@PathVariable UUID bundleId,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            ReservationDTO reservationDTO = reservationService.createReservation(bundleId, accessToken);
            return ResponseEntity.ok(reservationDTO);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (BundleAlreadyReservedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
     * Returns all pending reservation for the authenticated user
     * @param authToken A bearer access token
     * @return A ResponseEntity that returns 200 if successful
     *        500 if an error occurs
     */
    @Operation(summary = "Get all reservations for a user")
    @GetMapping
    public ResponseEntity<?> getReservations(@RequestParam(name = "status", defaultValue = "RESERVED", required = false) CollectionStatus status,
                                             @RequestHeader("Authorization") String authToken) {
        try {
            String accessToken = authToken.replace("Bearer ", "");

            List<ReservationDTO> reservationList = reservationService.getReservations(accessToken, status);
            return ResponseEntity.ok(reservationList);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Returns all pending reservations for the authenticated vendor
     * @param authToken A bearer access token
     * @return A ResponseEntity that returns 200 if successful
     *        401 if unauthorized
     *        500 if an other error occurs
     */
    @Operation(summary = "Get all pending reservations for a vendor")
    @GetMapping("/vendor")
    public ResponseEntity<?> getReservationsForVendor(@RequestHeader("Authorization") String authToken) {
        try {
            String accessToken = authToken.replace("Bearer ", "");
            List<ReservationVendorDTO> reservationList = reservationService.getReservationsForVendor(accessToken);
            return ResponseEntity.ok(reservationList);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Deletes a reservation by ID
     * @param reservationId of the reservation to be deleted
     * @param authToken A bearer access token
     * @return A ResponseEntity that returns 204 if deletion is successful
     *        404 is reservation is not found
     *        401 if unauthorized to delete reservation
     *        500 for other errors
     */
    @Operation(summary = "Delete a reservation using the UUID")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable UUID reservationId, @RequestHeader("Authorization") String authToken) {
        try {
            String accessToken = authToken.replace("Bearer ", "");
            reservationService.deleteReservation(reservationId, accessToken);
            return ResponseEntity.noContent().build();
        }

        catch (ReservationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Returns claim code for a reservation
     * @param reservationId of the reservation to get claim code from
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 200 if  successful
     *         404 is reservation is not found
     *         401 if unauthorized
     *         500 for other errors
     */
    @Operation(summary = "Get a claim code for a reservation")
    @GetMapping("/claimcode/{reservationId}")
    public ResponseEntity<?> getClaimCode(@PathVariable UUID reservationId, @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            ClaimCodeDTO claimCodeDTO = reservationService.getClaimCode(reservationId, accessToken);
            return ResponseEntity.ok(claimCodeDTO);
        }

        catch (ReservationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Using the claim code the vendor can verify a reservation
     * @param claimCodeDTO Request body containing the claim code
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 204 if successful
     *        404 if bundle is not found
     *        401 if unauthorized
     *        500 for other errors
     */
    @Operation(summary = "Allows a vendor to verify a reservation, marking it completed")
    @PostMapping("/claimcode")
    public ResponseEntity<?> checkClaimCode(@Valid @RequestBody ClaimCodeDTO claimCodeDTO, @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            reservationService.checkClaimCode(claimCodeDTO, accessToken);
            return ResponseEntity.noContent().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (BundleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Loads seeded data
     * @param authHeader A bearer access token
     * @param reservations list of ReservationSeedDTOs
     * @return A ResponseEntity that returns 204 if successful
     *        401 if unauthorized
     *        500 for other errors
     */
    @Operation(summary = "Allows bulk transfer of reservation data")
    @PostMapping("/internal")
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody List<ReservationSeedDTO> reservations) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            reservationService.loadSeededData(accessToken, reservations);
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

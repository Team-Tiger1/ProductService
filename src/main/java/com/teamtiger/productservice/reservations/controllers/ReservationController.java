package com.teamtiger.productservice.reservations.controllers;

import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.exceptions.BundleAlreadyReservedException;
import com.teamtiger.productservice.reservations.exceptions.ReservationNotFoundException;
import com.teamtiger.productservice.reservations.models.ReservationDTO;
import com.teamtiger.productservice.reservations.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

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
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get all pending reservations for a user")
    @GetMapping
    public ResponseEntity<?> getReservations(@RequestHeader("Authorization") String authToken) {
        try {
            String accessToken = authToken.replace("Bearer ", "");
            List<ReservationDTO> reservationList = reservationService.getReservations(accessToken);
            return ResponseEntity.ok(reservationList);
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

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



}

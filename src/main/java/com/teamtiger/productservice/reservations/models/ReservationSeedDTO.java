package com.teamtiger.productservice.reservations.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "Request body for seeding reservation data")
/**
 * DTO for bulk importing reservation records
 */
public class ReservationSeedDTO {

    @NotNull
    @Schema(description = "Reservation ID")
    private UUID reservationId;

    @NotNull
    @Schema(description = "Bundle ID")
    private UUID bundleId;

    @NotNull
    @Schema(description = "User ID")
    private UUID userId;

    @NotNull
    @Schema(
            description = "Reservation status",
            implementation = CollectionStatus.class
    )
    private CollectionStatus status;

    @NotNull
    @Schema(description = "Time the reservation was made")
    private LocalDateTime timeReserved;

    @NotNull
    @Schema(description = "Time the reservation was collected")
    private LocalDateTime timeCollected;


}

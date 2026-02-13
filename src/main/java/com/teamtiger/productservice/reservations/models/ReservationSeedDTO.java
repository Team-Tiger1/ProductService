package com.teamtiger.productservice.reservations.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
//DTO for bulk importing reservation records
public class ReservationSeedDTO {

    @NotNull
    private UUID reservationId;

    @NotNull
    private UUID bundleId;

    @NotNull
    private UUID userId;

    @NotNull
    private CollectionStatus status;

    @NotNull
    private LocalDateTime timeReserved;

    @NotNull
    private LocalDateTime timeCollected;


}

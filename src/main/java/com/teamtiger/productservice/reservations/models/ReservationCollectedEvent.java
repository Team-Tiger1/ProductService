package com.teamtiger.productservice.reservations.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCollectedEvent ( //Tied to Userservice (RabbitMQ)

    UUID userId,
    LocalDateTime reservationCollected
){}

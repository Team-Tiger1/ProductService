package com.teamtiger.productservice.reservations.entities;


import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "idx_bundle_id", columnList = "bundle_id"),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
public class Reservation {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "reservation_id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "bundle_id", unique = true)
    private Bundle bundle;

    @Column(name = "user_id")
    private UUID userId;

    @Positive
    private double amount_due;

    @Enumerated(EnumType.STRING)
    private CollectionStatus status;

    private LocalDateTime timeReserved;
    private LocalDateTime timeCollected;





}

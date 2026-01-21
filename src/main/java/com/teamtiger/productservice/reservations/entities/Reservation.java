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
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "reservation_id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "bundle_id", unique = true)
    private Bundle bundle;


    @Positive
    private int amount_due;

    @Enumerated(EnumType.STRING)
    private CollectionStatus status;

    private LocalDateTime timeReserved;
    private LocalDateTime timeCollected;





}

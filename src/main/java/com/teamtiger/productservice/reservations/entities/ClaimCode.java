package com.teamtiger.productservice.reservations.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "claim_codes", indexes = {@Index(name = "idx_claim_code", columnList = "claim_code")})
public class ClaimCode {

    @Id
    @Column(name = "reservation_id")
    private UUID reservationId;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reservation_id")
    @MapsId
    private Reservation reservation;

    @Column(name = "claim_code", nullable = false, unique = true)
    private String claimCode;

}

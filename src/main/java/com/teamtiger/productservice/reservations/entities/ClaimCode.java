package com.teamtiger.productservice.reservations.entities;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "claim_codes", indexes = {@Index(name = "idx_claim_code", columnList = "claim_code")})
public class ClaimCode {

    @Id
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "claim_code", nullable = false, unique = true)
    private String claimCode;

}

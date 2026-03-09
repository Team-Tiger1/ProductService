package com.teamtiger.productservice.reservations.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing  reservation returned to vendor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationVendorDTO {

    private UUID bundleId;
    private UUID reservationId;
    private String bundleName;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private double amountDue;

}

package com.teamtiger.productservice.reservations.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
//DTO representing  reservation returned to vendor
public class ReservationVendorDTO {

    private UUID bundleId;
    private UUID reservationId;
    private String bundleName;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;
    private double amountDue;

}

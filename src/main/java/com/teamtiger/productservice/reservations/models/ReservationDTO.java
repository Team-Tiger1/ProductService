package com.teamtiger.productservice.reservations.models;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import lombok.*;

import java.util.UUID;

/**
 * DTO representing a bundle reservation
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private UUID reservationId;
    private String vendorName;
    private String streetAddress;
    private String postcode;
    private BundleDTO bundle;

}

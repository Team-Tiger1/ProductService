package com.teamtiger.productservice.reservations.models;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a bundle reservation
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private UUID reservationId;
    private BundleDTO bundle;

}

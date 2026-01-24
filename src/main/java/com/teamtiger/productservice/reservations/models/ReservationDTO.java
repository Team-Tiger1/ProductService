package com.teamtiger.productservice.reservations.models;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReservationDTO {

    private UUID reservationId;
    private BundleDTO bundle;

}

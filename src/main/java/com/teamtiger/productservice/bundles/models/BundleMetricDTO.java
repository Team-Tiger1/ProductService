package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Data;

/**
 * //Used to return figures about bundle reservations to client
 */
@Builder
@Data
public class BundleMetricDTO {

    private Integer numNoShows;
    private Integer numCollected;
    private Integer numExpired;

}

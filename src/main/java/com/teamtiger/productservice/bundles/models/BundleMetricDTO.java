package com.teamtiger.productservice.bundles.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to return figures about bundle reservations to client
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleMetricDTO {

    private Integer numNoShows;
    private Integer numCollected;
    private Integer numExpired;

}

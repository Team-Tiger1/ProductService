package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
//Used to return figures about bundle reservations to client
public class BundleMetricDTO {

    private Integer numNoShows;
    private Integer numCollected;
    private Integer numExpired;

}

package com.teamtiger.productservice.bundles.models;

import lombok.Builder;

@Builder
public class BundleMetricDTO {

    private Integer numNoShows;
    private Integer numCollected;
    private Integer numExpired;

}

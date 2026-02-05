package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BundleMetricDTO {

    private Integer numNoShows;
    private Integer numCollected;
    private Integer numExpired;

}

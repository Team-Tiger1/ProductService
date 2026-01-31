package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ShortBundleDTO {

    private UUID bundleId;
    private String bundleName;
    private double price;

}

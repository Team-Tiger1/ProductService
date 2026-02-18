package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Represents information about a past bundle
 */
@Getter
@Builder
public class PastBundleDTO {

    private String bundleName;
    private LocalDateTime date;
    private double amountDue;
    private String status; //Collection Status

}

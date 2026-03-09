package com.teamtiger.productservice.bundles.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents information about a past bundle
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastBundleDTO {

    private String bundleName;
    private LocalDateTime date;
    private double amountDue;
    private String status; //Collection Status

}

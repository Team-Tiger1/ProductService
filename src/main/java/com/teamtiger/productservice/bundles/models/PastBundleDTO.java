package com.teamtiger.productservice.bundles.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents information about a past bundle
 */
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PastBundleDTO {

    private UUID bundleId;
    private String bundleName;
    private LocalDateTime date;
    private double amountDue;
    private String status; //Collection Status
    private double weight;

}

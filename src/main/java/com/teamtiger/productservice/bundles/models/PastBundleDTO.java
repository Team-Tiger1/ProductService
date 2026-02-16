package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.reservations.models.CollectionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PastBundleDTO {

    private String bundleName;
    private LocalDateTime date;
    private double amountDue;
    private CollectionStatus status;

}

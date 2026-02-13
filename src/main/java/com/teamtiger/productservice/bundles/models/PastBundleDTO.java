package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PastBundleDTO {

    private String bundleName;
    private LocalDateTime date;
    private double amountDue;
    private String status; //Colection Status

}

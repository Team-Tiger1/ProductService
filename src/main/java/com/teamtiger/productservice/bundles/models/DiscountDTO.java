package com.teamtiger.productservice.bundles.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscountDTO {

    private int startDiscount;
    private int endDiscount;
    private double collectionRate;

}

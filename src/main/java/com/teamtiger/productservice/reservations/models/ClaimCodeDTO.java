package com.teamtiger.productservice.reservations.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO used to represent a claim Code for verifying and collecting a reservation
 */
@Builder
@Getter
@AllArgsConstructor
public class ClaimCodeDTO {

    @NotBlank
    private String claimCode;

}

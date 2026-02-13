package com.teamtiger.productservice.reservations.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
//DTO used to represent a claim Code for verifying and collecting a reservation
public class ClaimCodeDTO {

    @NotBlank
    private String claimCode;

}

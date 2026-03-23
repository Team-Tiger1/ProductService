package com.teamtiger.productservice.reservations.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO used to represent a claim Code for verifying and collecting a reservation
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body containing a claim code")
public class ClaimCodeDTO {

    @NotBlank
    @Schema(description = "Claim code")
    private String claimCode;

}

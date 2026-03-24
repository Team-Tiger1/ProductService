package com.teamtiger.productservice.bundles.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for editing a bundle")
public class EditBundleDTO {
    @Schema(description = "Bundle name")
    private String name;
    @Schema(description = "Bundle description")
    private String description;
    @Schema(description = "Bundle price")
    private Double price;
    @Schema(description = "Collection start time")
    private LocalDateTime collectionStart;
    @Schema(description = "Collection end time")
    private LocalDateTime collectionEnd;

}

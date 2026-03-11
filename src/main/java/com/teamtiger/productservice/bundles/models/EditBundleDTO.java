package com.teamtiger.productservice.bundles.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditBundleDTO {

    private String name;
    private String description;
    private Double price;
    private LocalDateTime collectionStart;
    private LocalDateTime collectionEnd;

}

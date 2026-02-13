package com.teamtiger.productservice.bundles.models;

import com.teamtiger.productservice.bundles.entities.BundleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
//Returned to vendor when they create a Bundle
public class CreateBundleDTO {

    @NotBlank
    private String name;
    private String description;

    @NotNull
    @NotEmpty
    private List<UUID> productList;

    @NotNull
    private double price;

    @NotNull
    private BundleCategory category;

    @NotNull
    private LocalDateTime collectionStart;

    @NotNull
    private LocalDateTime collectionEnd;


}

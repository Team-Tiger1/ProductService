package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;

import java.util.UUID;

public interface BundleService {

    BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken);

    void deleteBundle(UUID bundleId, String accessToken);
}

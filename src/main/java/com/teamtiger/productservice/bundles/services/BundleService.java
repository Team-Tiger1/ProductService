package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.BundleSeedDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;

import java.util.List;
import java.util.UUID;

public interface BundleService {

    BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken);

    void deleteBundle(UUID bundleId, String accessToken);

    List<BundleDTO> getVendorBundles(UUID vendorId);

    List<BundleDTO> getOwnBundles(String accessToken);

    void loadSeededData(String accessToken, List<BundleSeedDTO> bundles);

}

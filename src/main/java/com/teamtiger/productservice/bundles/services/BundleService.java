package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;

public interface BundleService {

    BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken);

}

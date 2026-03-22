package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.bundles.models.*;

import java.util.List;
import java.util.UUID;

/**
 * Defines different operations for the management of Bundles
 */
public interface BundleService {

    BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken);

    void deleteBundle(UUID bundleId, String accessToken);

    List<ShortBundleDTO> getVendorBundles(UUID vendorId);

    List<BundleDTO> getOwnBundles(String accessToken);

    void loadSeededData(String accessToken, List<BundleSeedDTO> bundles);

    List<ShortBundleDTO> getAllBundles(int limit, int offset);

    BundleDTO getDetailedBundle(String accessToken, UUID bundleId);

    BundleMetricDTO getBundleMetrics(String accessToken, String timePeriod);

    Integer getNumBundlePosted(String accessToken);

    List<PastBundleDTO> getPastBundles(String accessToken, String period);

    void updateBundle(String accessToken, UUID bundleId, EditBundleDTO editBundleDTO);

    List<DiscountDTO> getDiscountSellThroughRate(String accessToken);

}

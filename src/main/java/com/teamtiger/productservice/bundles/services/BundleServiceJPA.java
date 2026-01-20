package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BundleServiceJPA implements BundleService {

    private final BundleRepository bundleRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ProductRepository productRepository;

    @Override
    public BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);


        List<Product> products = productRepository.findAllById(createBundleDTO.getProductList());
        double retailPrice = 0;
        for(Product product : products) {
            retailPrice += product.getRetailPrice();
        }

        Bundle bundle = Bundle.builder()
                .name(createBundleDTO.getName())
                .description(createBundleDTO.getDescription())
                .price(createBundleDTO.getPrice())
                .retailPrice(retailPrice)
                .vendorId(vendorId)
                .category(createBundleDTO.getCategory())
                .collectionStart(createBundleDTO.getCollectionStart())
                .collectionEnd(createBundleDTO.getCollectionEnd())
                .build();

        Bundle savedBundle = bundleRepository.save(bundle);

        return BundleMapper.toDTO(savedBundle);
    }

    @Override
    public void deleteBundle(UUID bundleId, String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Bundle savedBundle = bundleRepository.findById(bundleId)
                .orElseThrow(BundleNotFoundException::new);

        if(!vendorId.equals(savedBundle.getVendorId())) {
            throw new VendorAuthorizationException();
        }

        bundleRepository.deleteById(bundleId);
    }

    private static class BundleMapper {

        public static BundleDTO toDTO(Bundle entity) {
            return BundleDTO.builder()
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .price(entity.getPrice())
                    .retailPrice(entity.getRetailPrice())
                    .vendorId(entity.getVendorId())
                    .category(entity.getCategory())
                    .collectionStart(entity.getCollectionStart())
                    .collectionEnd(entity.getCollectionEnd())
                    .build();
        }
    }
}

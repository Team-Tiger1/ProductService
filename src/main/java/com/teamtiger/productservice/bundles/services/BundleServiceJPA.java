package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.BundleSeedDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.models.ShortBundleDTO;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
                .products(products)
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

        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if(!role.equals("VENDOR")) {
            throw new VendorAuthorizationException();
        }

        Bundle savedBundle = bundleRepository.findById(bundleId)
                .orElseThrow(BundleNotFoundException::new);

        if(!vendorId.equals(savedBundle.getVendorId())) {
            throw new VendorAuthorizationException();
        }

        bundleRepository.deleteById(bundleId);
    }

    @Override
    public List<ShortBundleDTO> getVendorBundles(UUID vendorId) {

        List<Bundle> bundleList = bundleRepository.findAvailableBundlesByVendor(vendorId);

        return bundleList.stream()
                .map(entity -> ShortBundleDTO.builder()
                        .bundleId(entity.getId())
                        .price(entity.getPrice())
                        .bundleName(entity.getName())
                        .category(entity.getCategory())
                        .build()
                ).toList();
    }

    @Override
    public List<BundleDTO> getOwnBundles(String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new VendorAuthorizationException();
        }

        List<Bundle> bundles = bundleRepository.findAllByVendorId(vendorId);
        return bundles.stream()
                .map(BundleMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public void loadSeededData(String accessToken, List<BundleSeedDTO> bundles) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        List<Bundle> entities = bundles.stream()
                .map(dto -> {

                    List<Product> productList = productRepository.findAllById(dto.getProductIds());

                    //Get Aggregate Allergies from Products
                    Set<Allergy> allergies = productList.stream()
                            .map(Product::getAllergies)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());

                    //Calculate Retail Price from Products
                    double retailPrice = productList.stream()
                                    .mapToDouble(Product::getRetailPrice)
                                    .sum();

                    return Bundle.builder()
                            .id(dto.getBundleId())
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .price(dto.getPrice())
                            .retailPrice(retailPrice)
                            .products(productList)
                            .allergies(allergies)
                            .vendorId(dto.getVendorId())
                            .category(dto.getCategory())
                            .postingTime(dto.getPostingTime())
                            .collectionStart(dto.getCollectionStart())
                            .collectionEnd(dto.getCollectionEnd())
                            .build();
                })
                .toList();



        bundleRepository.saveAll(entities);

    }

    @Override
    public List<ShortBundleDTO> getAllBundles(int limit, int offset) {

        Pageable pageable = PageRequest.of(offset, limit);
        List<Bundle> bundles = bundleRepository.findAvailableBundles(pageable);

        return bundles.stream()
                .map(entity -> ShortBundleDTO.builder()
                        .bundleId(entity.getId())
                        .price(entity.getPrice())
                        .build())
                .toList();
    }

    private static class BundleMapper {

        public static BundleDTO toDTO(Bundle entity) {

            List<GetProductDTO> productDTOs = entity.getProducts().stream()
                    .map(ProductMapper::toDTO)
                    .toList();

            return BundleDTO.builder()
                    .name(entity.getName())
                    .bundleId(entity.getId())
                    .description(entity.getDescription())
                    .price(entity.getPrice())
                    .retailPrice(entity.getRetailPrice())
                    .vendorId(entity.getVendorId())
                    .productList(productDTOs)
                    .category(entity.getCategory())
                    .collectionStart(entity.getCollectionStart())
                    .collectionEnd(entity.getCollectionEnd())
                    .build();
        }
    }
}

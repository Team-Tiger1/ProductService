package com.teamtiger.productservice.bundles.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.entities.BundleProduct;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.*;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.repositories.AllergyRepository;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BundleServiceJPA implements BundleService {

    private final BundleRepository bundleRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ProductRepository productRepository;
    private final AllergyRepository allergyRepository;

    @Override
    public BundleDTO createBundle(CreateBundleDTO createBundleDTO, String accessToken) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        //Count times product appears
        Map<UUID, Long> occurenceMap = createBundleDTO.getProductList().stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));


        List<Product> products = productRepository.findAllById(createBundleDTO.getProductList());
        double retailPrice = 0;
        for(Product product : products) {
            retailPrice += product.getRetailPrice() * occurenceMap.get(product.getId());
        }


        Set<AllergyType> bundleAllergyTypes = products.stream()
                .flatMap(p -> p.getAllergies().stream())
                .map(Allergy::getAllergyType)
                .collect(Collectors.toSet());

        Set<Allergy> bundleAllergies = bundleAllergyTypes.stream()
                .map(type -> allergyRepository.findByAllergyType(type).get())
                .collect(Collectors.toSet());

        Bundle bundle = Bundle.builder()
                .name(createBundleDTO.getName())
                .description(createBundleDTO.getDescription())
                .price(createBundleDTO.getPrice())
                .retailPrice(retailPrice)
                .vendorId(vendorId)
                .category(createBundleDTO.getCategory())
                .collectionStart(createBundleDTO.getCollectionStart())
                .collectionEnd(createBundleDTO.getCollectionEnd())
                .allergies(bundleAllergies)
                .build();

        bundle = bundleRepository.save(bundle);

        //Add products
        for(Product product : products) {
            int quantity = occurenceMap.get(product.getId()).intValue();
            bundle.addProduct(product, quantity);
        }

        bundle = bundleRepository.save(bundle);


        return BundleMapper.toDTO(bundle);
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
                        .allergens(entity.getAllergies().stream()
                                .map(Allergy::getAllergyType)
                                .collect(Collectors.toSet()))
                        .collectionStart(entity.getCollectionStart())
                        .collectionEnd(entity.getCollectionEnd())
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

        Set<UUID> allProductIds = bundles.stream()
                .flatMap(dto -> dto.getProductIds().stream())
                .collect(Collectors.toSet());

        Map<UUID, Product> productMap = productRepository.findAllById(allProductIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Bundle> entities = bundles.stream()
                .map(dto -> {

                    //Count times product appears
                    Map<UUID, Long> occurenceMap = dto.getProductIds().stream()
                            .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

                    List<Product> productList = dto.getProductIds().stream()
                            .map(productMap::get)
                            .filter(Objects::nonNull)
                            .toList();

                    //Get Aggregate Allergies from Products
                    Set<Allergy> allergies = productList.stream()
                            .map(Product::getAllergies)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());

                    //Calculate Retail Price from Products
                    double retailPrice = occurenceMap.entrySet().stream()
                                    .mapToDouble(entry -> {
                                        Product product = productMap.get(entry.getKey());
                                        return product.getRetailPrice() * entry.getValue();
                                    })
                                    .sum();

                    Bundle bundle = Bundle.builder()
                            .id(dto.getBundleId())
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .price(dto.getPrice())
                            .retailPrice(retailPrice)
                            .allergies(allergies)
                            .vendorId(dto.getVendorId())
                            .category(dto.getCategory())
                            .postingTime(dto.getPostingTime())
                            .collectionStart(dto.getCollectionStart())
                            .collectionEnd(dto.getCollectionEnd())
                            .build();

                    //Add products
                    for(UUID productId : occurenceMap.keySet()) {
                        Product product = productMap.get(productId);
                        if(product == null) continue;

                        int quantity = occurenceMap.get(productId).intValue();
                        bundle.addProduct(product, quantity);
                    }

                    return bundle;
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
                        .bundleName(entity.getName())
                        .category(entity.getCategory())
                        .bundleId(entity.getId())
                        .price(entity.getPrice())
                        .build())
                .toList();
    }


    @Override
    public BundleDTO getDetailedBundle(String accessToken, UUID bundleId) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        Bundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(BundleNotFoundException::new);

        return BundleMapper.toDTO(bundle);

    }

    @Override
    public BundleMetricDTO getBundleMetrics(String accessToken, String timePeriod) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime period = switch (timePeriod) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> now.minusWeeks(1);
        };

        List<Object[]> bundleMetrics = bundleRepository.countBundlesByVendorId(vendorId, period);

        Long numExpiredBundles = bundleRepository.countPreviousExpiredBundlesByVendor(vendorId, period);

        Long noShows = 0L;
        Long collected = 0L;
        for(Object[] group : bundleMetrics) {
            switch (group[0].toString()) {
                case "NO_SHOW":
                    noShows = (long) group[1];
                    break;
                case "COLLECTED":
                    collected = (long) group[1];
                    break;
            }
        }

        return BundleMetricDTO.builder()
                .numCollected(collected.intValue())
                .numNoShows(noShows.intValue())
                .numExpired(numExpiredBundles.intValue())
                .build();
    }

    private static class BundleMapper {

        public static BundleDTO toDTO(Bundle entity) {

            List<BundleProductDTO> productDTOs = entity.getBundleProducts().stream()
                    .map(bp -> {
                        Product product = bp.getProduct();
                        return BundleProductDTO.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .quantity(bp.getQuantity())
                                .allergies(
                                        product.getAllergies().stream()
                                                .map(Allergy::getAllergyType)
                                                .collect(Collectors.toSet())
                                )

                                .price(product.getRetailPrice())
                                .build();
                    })
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
                    .allergies(entity.getAllergies().stream()
                                    .map(Allergy::getAllergyType)
                                    .collect(Collectors.toSet())
                    )
                    .build();
        }
    }
}

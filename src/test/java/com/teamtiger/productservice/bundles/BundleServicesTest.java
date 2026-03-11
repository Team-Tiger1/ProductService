package com.teamtiger.productservice.bundles;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.*;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.bundles.services.BundleServiceJPA;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;


/**
 * unit tests for the bundle service
 */
@ExtendWith(MockitoExtension.class)
public class BundleServicesTest {

    @Mock
    private BundleRepository bundleRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private ProductRepository productRepository;

//    @Mock
//    private AllergyRepository allergyRepository;



    @InjectMocks
    private BundleServiceJPA bundleService;

    private UUID testBundleId;
    private UUID testVendorId;
    private UUID testProductId;

    private Bundle mockBundle;
    private Product mockProduct;
    private CreateBundleDTO createBundleDTO;


    @BeforeEach
    void set_up() {

        testBundleId = UUID.randomUUID();
        testVendorId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        mockProduct = Product.builder()
                .id(testProductId)
                .name("Red velvet Cake")
                .retailPrice(3.75)
                .allergies(Set.of())
                .build();

        mockBundle = Bundle.builder()
                .id(testBundleId)
                .name("Sweet Treat Bundle")
                .description("test bundle desc")
                .price(4.71)
                .retailPrice(12.40)
                .vendorId(testVendorId)
                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .collectionStart(LocalDateTime.now().plusHours(1))
                .collectionEnd(LocalDateTime.now().plusHours(3))
                .allergies(Set.of())
                .bundleProducts(new HashSet<>()) //hashset so its immutable for addProducts
                .postingTime(LocalDateTime.now())
                .build();

        createBundleDTO = CreateBundleDTO.builder()
                .name("Sweet Treat Bundle")
                .description("test bundle description")
                .price(4.71)
//                .productList(List.of(UUID.randomUUID(), UUID.randomUUID())
                .productList(List.of(testProductId, UUID.randomUUID(), UUID.randomUUID()))

                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .collectionEnd(LocalDateTime.now())
                .collectionStart(LocalDateTime.now().plusHours(3))

                .build();
    }



    /**
     * createBundle test 1
     * success
     * returns BundleDTO with correct fields
     */
    @Test
    public void testCreateBundle_Success() {

        //add the behavior to the mock objects
        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        when(productRepository.findAllById(anyList())).thenReturn(List.of(mockProduct));
        //when(allergyRepository.findByAllergyType(any())).thenReturn(Optional.empty());
        when(bundleRepository.save(any(Bundle.class))).thenReturn(mockBundle);

        BundleDTO result = bundleService.createBundle(createBundleDTO, "Bearer vendorAccessToken123");

        //ensure the returned DTO has the bundle and vendor ids
        assertThat(result).isNotNull();
        assertThat(result.getBundleId()).isEqualTo(testBundleId);
        assertThat(result.getVendorId()).isEqualTo(testVendorId);


        //ensure the name and price are as expected from the setUp
        assertThat(result.getName()).isEqualTo("Sweet Treat Bundle");
        assertThat(result.getPrice()).isEqualTo(4.71);

        verify(bundleRepository, times(2)).save(any(Bundle.class));
    }


    /**
     * createBundle test 2
     * a non-vendor token tries to create a bundle
     * throws VendorAuthorizationException
     */
    @Test
    public void testCreateBundle_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() -> bundleService.createBundle(createBundleDTO, "Bearer consumerToken123"))
                .isInstanceOf(VendorAuthorizationException.class);

        //make sure we never use the database if auth fails
        verify(bundleRepository, never()).save(any(Bundle.class));

        //
    }


    /**
     * createBundle test 3
     * whitespace is trimmed in product names
     */
    @Test
    public void testCreateBundle_Trim() {

        CreateBundleDTO paddedName = CreateBundleDTO.builder()
                .name("  Sweet Treat Bundle       ")
                .description("test bundle desc")
                .price(4.71)
                .productList(List.of(testProductId))
                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .collectionStart(LocalDateTime.now().plusHours(1))
                .collectionEnd(LocalDateTime.now().plusHours(3))
                .build();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(productRepository.findAllById(anyList())).thenReturn(List.of(mockProduct));
        when(bundleRepository.save(any(Bundle.class))).thenAnswer(invocation -> {
            Bundle savedBundle = invocation.getArgument(0);
            //confirm the name was trimmed before save
            assertThat(savedBundle.getName()).isEqualTo("Sweet Treat Bundle");
            return mockBundle;
        });

        bundleService.createBundle(paddedName, "Bearer vendorAccessToken123");

        verify(bundleRepository, atLeastOnce()).save(any(Bundle.class));
    }



    /**
     * deleteBundle test 1
     * success
     * no exception thrown
     */
    @Test
    public void testDeleteBundle_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.of(mockBundle));
        doNothing().when(bundleRepository).deleteById(testBundleId);


        bundleService.deleteBundle(testBundleId, "Bearer vendorAccessToken123");

        verify(bundleRepository).deleteById(testBundleId);
    }


    /**
     * deleteBundle test 2
     * bundle does not exist in the database, so cant be deleted
     * BundleNotFoundException thrown
     */
    @Test
    public void testDeleteBundle_BundleNotFound() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");


        //bundle cannot be found
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bundleService.deleteBundle(testBundleId, "Bearer vendorAccessToken123"))
                .isInstanceOf(BundleNotFoundException.class);

        //ensure never use deleteById if bundle doesn't exist
        verify(bundleRepository, never()).deleteById(any());
    }


    /**
     * deleteBundle test 3
     * a vendor tries to delete another vendors bundle
     * VendorAuthorizationException thrown
     */
    @Test
    public void testDeleteBundle_WrongVendor() {

        //the other vendor
        UUID differentVendorId = UUID.randomUUID();

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(differentVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        //bundle belongs to testVendorId not differentVendorId
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.of(mockBundle));

        assertThatThrownBy(() -> bundleService.deleteBundle(testBundleId, "Bearer wrongVendorToken123"))
                .isInstanceOf(VendorAuthorizationException.class);

        verify(bundleRepository, never()).deleteById(any());
    }


    /**
     * deleteBundle test 4
     * a nonvendor tries
     * VendorAuthorizationException thrown
     */
    @Test
    public void testDeleteBundle_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() -> bundleService.deleteBundle(testBundleId, "Bearer consumerToken123"))
                .isInstanceOf(VendorAuthorizationException.class);

        verify(bundleRepository, never()).deleteById(any());
    }



    /**
     * getVendorBundles test 1
     * success
     * returns a list bundles for a given vendor
     */
    @Test
    public void testGetVendorBundles_Success() {

        when(bundleRepository.findAvailableBundlesByVendor(testVendorId)).thenReturn(List.of(mockBundle));

        List<ShortBundleDTO> result = bundleService.getVendorBundles(testVendorId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        //ensure the returned bundle has the correct ids and name
        assertThat(result.get(0).getBundleId()).isEqualTo(testBundleId);
        assertThat(result.get(0).getVendorId()).isEqualTo(testVendorId);
        assertThat(result.get(0).getBundleName()).isEqualTo("Sweet Treat Bundle");

        verify(bundleRepository).findAvailableBundlesByVendor(testVendorId);
    }


    /**
     * getVendorBundles test 2
     * vendor has no bundles
     * returns empty list
     */
    @Test
    public void testGetVendorBundles_NoBundles() {

        when(bundleRepository.findAvailableBundlesByVendor(testVendorId)).thenReturn(List.of());

        List<ShortBundleDTO> result = bundleService.getVendorBundles(testVendorId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }



    /**
     * getOwnBundles test 1
     * success
     * returns a list of bundledtoes
     */
    @Test
    public void testGetOwnBundles_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.findAvailableBundlesByVendor(testVendorId)).thenReturn(List.of(mockBundle));

        List<BundleDTO> result = bundleService.getOwnBundles("Bearer vendorAccessToken123");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); //one bundle returned, the list has size 1

        //ensure the returned bundle has the correct ids and name
        assertThat(result.get(0).getBundleId()).isEqualTo(testBundleId);
        assertThat(result.get(0).getVendorId()).isEqualTo(testVendorId);

        assertThat(result.get(0).getName()).isEqualTo("Sweet Treat Bundle");

        verify(bundleRepository).findAvailableBundlesByVendor(testVendorId);
    }


    /**
     * getOwnBundles test 2
     * a nonvendor tries
     * VendorAuthorizationException thrown
     */
    @Test
    public void testGetOwnBundles_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() -> bundleService.getOwnBundles("Bearer consumerToken123"))
                .isInstanceOf(VendorAuthorizationException.class);

        verify(bundleRepository, never()).findAvailableBundlesByVendor(any());
    }


    /**
     * getOwnBundles test 3
     * vendor has no active bundles
     * returns empty list
     */
    @Test
    public void testGetOwnBundles_NoBundles() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.findAvailableBundlesByVendor(testVendorId)).thenReturn(List.of());

        List<BundleDTO> result = bundleService.getOwnBundles("Bearer vendorAccessToken123");

        assertThat(result).isNotNull() ;
        assertThat(result).isEmpty();
    }


}
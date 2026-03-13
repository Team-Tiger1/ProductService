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




    /**
     * getAllBundles test 1
     * success
     * returns a list of all available bundles
     */

    @Test
    public void testGetAllBundles_Success() {

        when(bundleRepository.findAvailableBundles(any())).thenReturn(List.of(mockBundle));

        List<ShortBundleDTO> result = bundleService.getAllBundles(50, 0);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBundleId()).isEqualTo(testBundleId);


        verify(bundleRepository).findAvailableBundles(any());
    }


    /**
     * getAllBundles test 2
     * no bundles exist
     * return empty list
     */
    @Test
    public void testGetAllBundles_NoBundles() {

        when(bundleRepository.findAvailableBundles(any())).thenReturn(List.of());

        List<ShortBundleDTO> result = bundleService.getAllBundles(50, 0);


        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }



    /**
     * getDetailedBundle test 1
     * success
     * returns a BundleDTO with right fields
     */

    @Test
    public void testGetDetailedBundle_Success() {

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.of(mockBundle));

        BundleDTO result = bundleService.getDetailedBundle("Bearer userAccessToken123", testBundleId);

        assertThat(result).isNotNull();
        assertThat(result.getBundleId()).isEqualTo(testBundleId);
        assertThat(result.getVendorId()).isEqualTo(testVendorId);
        //make sure details align with the setUp
        assertThat(result.getName()).isEqualTo("Sweet Treat Bundle");
        assertThat(result.getPrice()).isEqualTo(4.71);

        verify(bundleRepository).findById(testBundleId);
    }


    /**
     * getDetailedBundle test 2
     * a nonuser token tries to get bundle info
     * AuthorizationException thrown
     */
    @Test
    public void testGetDetailedBundle_NonUserRole() {

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> bundleService.getDetailedBundle("Bearer vendorAccessToken123", testBundleId))
                .isInstanceOf(AuthorizationException.class);

        //make sure it never querys for the bundle if the auth fails
        verify(bundleRepository, never()).findById(any());

    }


    /**
     * getDetailedBundle test 3
     * bundle does not exist
     * BundleNotFoundException thrown
     */
    @Test
    public void testGetDetailedBundle_BundleNotFound() {

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //bundle cant be found
        when(bundleRepository.findById(testBundleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bundleService.getDetailedBundle("Bearer userAccessToken123", testBundleId))
                .isInstanceOf(BundleNotFoundException.class);
    }



    /**
     * getBundleMetrics test 1
     * success
     * returns BundleMetricDTO with the correct values
     */
    @Test
    public void testGetBundleMetrics_Success() {

        //mock repository returning 5 collected 2 no-shows
        Object[] collectedGroup =new Object[]{"COLLECTED",5L};
        Object[] noShowGroup= new Object[]{"NO_SHOW",2L};

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.countBundlesByVendorId(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of(collectedGroup, noShowGroup));

        when(bundleRepository.countPreviousExpiredBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(3L);

        BundleMetricDTO result = bundleService.getBundleMetrics("Bearer vendorAccessToken123", "week");

        assertThat(result).isNotNull();
        assertThat(result.getNumCollected()).isEqualTo(5);
        assertThat(result.getNumNoShows()).isEqualTo(2);
        assertThat(result.getNumExpired()).isEqualTo(3);
    }


    /**
     * getBundleMetrics test 2
     * nonvendor tries
     * AuthorizationException thrown
     */

    @Test
    public void testGetBundleMetrics_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        //get metrics for week
        assertThatThrownBy(() -> bundleService.getBundleMetrics("Bearer consumerToken123", "week"))
                .isInstanceOf(AuthorizationException.class);


        verify(bundleRepository, never()).countBundlesByVendorId(any(), any());
    }


    /**
     * getBundleMetrics test 3
     * 'day' is the time period
     * ensures that period is calculated as 1 day
     */
    @Test
    public void testGetBundleMetrics_DayPeriod() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.countBundlesByVendorId(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bundleRepository.countPreviousExpiredBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(0L);

        BundleMetricDTO result = bundleService.getBundleMetrics("Bearer vendorAccessToken123", "day");

        //just confirm it returns without throwing and with 0 counts
        assertThat(result).isNotNull();
        assertThat(result.getNumCollected()).isEqualTo(0);
        assertThat(result.getNumNoShows()).isEqualTo(0);
        assertThat(result.getNumExpired()).isEqualTo(0);
    }



    /**
     * getNumBundlePosted test 1
     * success
     */
    @Test
    public void testGetNumBundlePosted_Success() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
        when(bundleRepository.countPostedBundlesByVendor(testVendorId)).thenReturn(7L);

        Integer result = bundleService.getNumBundlePosted("Bearer vendorAccessToken123");

        assertThat(result).isEqualTo(7);

        verify(bundleRepository).countPostedBundlesByVendor(testVendorId);


    }


    /**
     * getNumBundlePosted test 2
     * nonvendor tries
     * AuthorizationException thrown
     */

    @Test
    public void testGetNumBundlePosted_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() ->bundleService.getNumBundlePosted("Bearer consumerToken123"))
                .isInstanceOf(AuthorizationException.class);

        verify(bundleRepository, never()).countPostedBundlesByVendor(any());
    }



    /**
     * getPastBundles test 1
     * success
     * returns a list of PastBundleDTOs
     */
    @Test
    public void testGetPastBundles_Success() {

        //mock one collected bundle and one expired bundle
        Object[] collectedEntry = new Object[]{CollectionStatus.COLLECTED, mockBundle};

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
//        when(bundleRepository.findPastBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
//                .thenReturn(Set.of(collectedEntry));
        when(bundleRepository.findExpiredBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of(mockBundle));
        List<Object[]> pastBundles = new ArrayList<>();
        pastBundles.add(collectedEntry);
        when(bundleRepository.findPastBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(pastBundles);

        List<PastBundleDTO> result = bundleService.getPastBundles("Bearer vendorAccessToken123", "week");

        assertThat(result).isNotNull();
        //one COLLECTED + one EXPIRED = 2 entries
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(PastBundleDTO::getStatus))
                .containsExactlyInAnyOrder("COLLECTED", "EXPIRED");
    }


    /**
     * getPastBundles test 2
     * reserved bundles are filtered out/not included in the results
     */
    @Test
    public void testGetPastBundles_ReservedBundles() {

        //reserved bundles should be skipped
        Object[] reservedEntry = new Object[]{CollectionStatus.RESERVED, mockBundle};

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");
//        when(bundleRepository.findPastBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
//                .thenReturn(List.of(reservedEntry));
        when(bundleRepository.findExpiredBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<Object[]> reservedBundles = new ArrayList<>();
        reservedBundles.add(reservedEntry);
        when(bundleRepository.findPastBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(reservedBundles);

        List<PastBundleDTO> result = bundleService.getPastBundles("Bearer vendorAccessToken123", "week");

        //reserved entries should be stripped, so the list should be empty
        assertThat(result).isEmpty();
    }


    /**
     * getPastBundles test 3
     * a nonvendor tries
     * AuthorizationException thrown
     */
    @Test
    public void testGetPastBundles_NonVendor() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("USER");

        assertThatThrownBy(() -> bundleService.getPastBundles("Bearer consumerToken123", "week"))
                .isInstanceOf(AuthorizationException.class);

        verify(bundleRepository, never()).findPastBundlesByVendor(any(), any());
    }


    /**
     * getPastBundles test 4
     * vendor has no past bundles in the given period
     * returns empty list
     */
    @Test
    public void testGetPastBundles_NoBundles() {

        when(jwtTokenUtil.getUuidFromToken(anyString())).thenReturn(testVendorId);
        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        when(bundleRepository.findPastBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bundleRepository.findExpiredBundlesByVendor(eq(testVendorId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<PastBundleDTO> result = bundleService.getPastBundles("Bearer vendorAccessToken123", "week");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }



    /**
     * loadSeededData test 1
     * success
     * no exception thrown + save is called
     */
    @Test
    public void testLoadSeededData_Success() {

//        BundleSeedDTO seedDTO = BundleSeedDTO.builder()
//                .name("Seeded Bundle")
//                .description("seed desc")
//                .price(1.99)
//                .productIds(List.of(testProductId))
//                .category(BundleCategory.SWEET_TREATS_DESSERTS)
//                .vendorId(testVendorId)
//                .collectionStart(LocalDateTime.now().plusHours(1))
//                .collectionEnd(LocalDateTime.now().plusHours(3))
//                .build();

        BundleSeedDTO seedDTO = new BundleSeedDTO(
                testBundleId,
                List.of(testProductId),
                Set.of(),
                testVendorId,
                "Seeded bundle",
                BundleCategory.SWEET_TREATS_DESSERTS,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now(),
                "seed desc",
                1.99
        );

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("INTERNAL");
        when(productRepository.findAllById(any())).thenReturn(List.of(mockProduct));
        when(bundleRepository.saveAll(anyList())).thenReturn(List.of(mockBundle));

        //no exception should be thrown
        bundleService.loadSeededData("Bearer internalToken123", List.of(seedDTO));

        verify(bundleRepository).saveAll(anyList());
    }


    /**
     * loadSeededData test 2
     * a non-internal token tries to load seeded data
     * AuthorizationException thrown
     */
    @Test
    public void testLoadSeededData_NonInternalRole() {

        when(jwtTokenUtil.getRoleFromToken(anyString())).thenReturn("VENDOR");

        assertThatThrownBy(() -> bundleService.loadSeededData("Bearer vendorToken123", List.of()))
                .isInstanceOf(AuthorizationException.class);

        verify(bundleRepository, never()).saveAll(anyList());
    }
}



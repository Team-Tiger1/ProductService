package com.teamtiger.productservice.bundles;

import com.teamtiger.productservice.bundles.exceptions.BundleNotFoundException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtiger.productservice.bundles.models.BundleMetricDTO;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import tools.jackson.databind.ObjectMapper;
import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.services.BundleService;
import com.teamtiger.productservice.products.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

//import com.teamtiger.productservice.bundles.models.DeleteBundleDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;


import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * unit tests for the bundle controller
 */
@SpringBootTest
public class BundleControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;


    @Autowired
    private ObjectMapper objectMapper;

    //private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BundleService bundleService;

    private CreateBundleDTO createBundleDTO;

    private BundleDTO bundleDTO;
    private BundleMetricDTO bundleMetricDTO;

    private UUID testBundleId;
    private UUID testVendorId;


    @BeforeEach
    void set_up() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        testBundleId = UUID.randomUUID();
        testVendorId = UUID.randomUUID();

        createBundleDTO = CreateBundleDTO.builder()
                .name("Sweet Treat Bundle")
                .description("test bundle description")
                .price(2.66)
                .productList(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .collectionEnd(LocalDateTime.now())
                .collectionStart(LocalDateTime.now().plusHours(3))

                .build();




        bundleDTO = BundleDTO.builder()
                .vendorId(testVendorId)
                .bundleId(testBundleId)
                .name("Sweet Treat Bundle")

                .price(2.66)
                .retailPrice(7.50)
                .category(BundleCategory.SWEET_TREATS_DESSERTS)
                .productList(List.of())
                .collectionStart(LocalDateTime.now().plusHours(1))
                .collectionEnd(LocalDateTime.now().plusHours(3))

                .build();

        bundleMetricDTO = BundleMetricDTO.builder()
                .numNoShows(3)
                .numCollected(23)
                .numExpired(6)
                .build();
    }


    /**
     * create bundles test 1
     * successful vendor bundle creation
     * should return 200
     */

    @Test
    public void testCreateBundle_Success_200() throws Exception {
        String requestBody = objectMapper.writeValueAsString(createBundleDTO);

        when(bundleService.createBundle(any(CreateBundleDTO.class), anyString()))
                .thenReturn(bundleDTO);

        //post request
        mockMvc.perform(post("/bundles")
                        .header("Authorization", "Bearer vendorAccessToken123")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //asset 200 response
                //ensure the response has the correct bundle id in
                .andExpect(jsonPath("$.vendorId").value(testVendorId.toString()))
                .andExpect(jsonPath("$.bundleId").value(testBundleId.toString()))
                //ensure the name and price are correct
                .andExpect(jsonPath("$.name").value("Sweet Treat Bundle"))
                .andExpect(jsonPath("$.price").value(2.66));


    }



    /**
     * create bundles test 2
     * a non-vendor token tries to create a bundle
     * 500 error
    */
    @Test
    public void testCreateBundle_NonVendorRole_500() throws Exception {

        String requestBody = objectMapper.writeValueAsString(createBundleDTO);

        // says that when createBundle() is called, throw exception.
        // service rejecting the request because the JWT token isn't a vendor
        when(bundleService.createBundle(any(CreateBundleDTO.class), anyString()))
                .thenThrow(new VendorAuthorizationException());

        //post request
        mockMvc.perform(post("/bundles")
                        .header("Authorization", "Bearer consumerToken123")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isInternalServerError()); //500 error

        //makes sure the service was called
        verify(bundleService).createBundle(any(CreateBundleDTO.class), anyString());
    }

    /**
     * create bundles test 3
     * 500 runtime error
     */

    @Test
    public void testCreateBundle_DBerror_500() throws Exception {

        String requestBody = objectMapper.writeValueAsString(createBundleDTO);

        doThrow(new RuntimeException("database error"))
                .when(bundleService)
                .createBundle(any(CreateBundleDTO.class), anyString());

        mockMvc.perform(post("/bundles")
                        .header("Authorization", "Bearer accessToken123")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bundleService).createBundle(any(CreateBundleDTO.class),anyString());

    }



    /**
     * delete bundles test1
     * delete bundles success
     * 204
     */

    @Test
    public void testDeleteBundle_Success_200() throws Exception {

        //String requestBody = objectMapper.writeValueAsString(deleteBundleDTO);

        doNothing().when(bundleService).deleteBundle(any(UUID.class), anyString());

        mockMvc.perform(delete("/bundles/{bundleId}", testBundleId)
                    .header("Authorization", "Bearer vendorAccessToken123"))
                    //.content(requestBody)
                    //.contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(bundleService).deleteBundle(eq(testBundleId), anyString());

    }


    /**
     * delete bundles test 2
     * bundle not found
     * 404
     */

    @Test
    public void testDeleteBundle_BundleNotFound_400() throws Exception {
        doThrow(new BundleNotFoundException())
                .when(bundleService).deleteBundle(any(UUID.class), anyString());

        mockMvc.perform(delete("/bundles/{bundleId}", testBundleId)
                    .header("Authorization", "Bearer vendorAccessToken123"))
            .andExpect(status().isNotFound());

        verify(bundleService).deleteBundle(eq(testBundleId), anyString());


    }


    /**
     * delete bundles test 3
     * incorrect vendor
     * 401
     */

    @Test
    public void testDeleteBundle_WrongVendor_400() throws Exception {

        doThrow(new VendorAuthorizationException())
            .when(bundleService)
            .deleteBundle(any(UUID.class), anyString());

        mockMvc.perform(delete("/bundles/{bundleId}", testBundleId)
                    .header("Authorization", "Bearer vendorAccessToken123"))
            .andExpect(status().isUnauthorized());

        verify(bundleService).deleteBundle(eq(testBundleId), anyString());

    }


    /**
     * delete bundles test 4
     * database error/internal error
     * 500
     */

    @Test
    public void testDeleteBundle_DBerror_500() throws Exception {
        doThrow(new RuntimeException("database error"))
            .when(bundleService)
            .deleteBundle(any(UUID.class), anyString());

        mockMvc.perform(delete("/bundles/{bundleId}", testBundleId)
                    .header("Authorization", "Bearer vendorAccessToken123"))
            .andExpect(status().isInternalServerError());

        verify(bundleService).deleteBundle(eq(testBundleId), anyString());


    }


    /**
     * getOwn Bundles test 1
     * success
     * 200
     */

    @Test
    public void testGetOwnBundle_Success_200() throws Exception {

        //String requestBody = objectMapper.writeValueAsString(deleteBundleDTO);

        when(bundleService.getOwnBundles(anyString())).thenReturn(List.of(bundleDTO));

        mockMvc.perform(get("/bundles/me")
                    .header("Authorization", "Bearer vendorAccessToken123"))
            .andExpect(status().isOk())
            //ensure the response has the correct bundle id in
            //need [] because the repose is a list this time
            .andExpect(jsonPath("$[0].vendorId").value(testVendorId.toString()))
            .andExpect(jsonPath("$[0].bundleId").value(testBundleId.toString()))
            //ensure the name and price are correct
            .andExpect(jsonPath("$[0].name").value("Sweet Treat Bundle"))
            .andExpect(jsonPath("$[0].price").value(2.66));

        verify(bundleService).getOwnBundles(anyString());

    }


    /**
     * getOwn Bundles test 2
     * unauthorised vendor
     * 401
     */

    @Test
    public void testGetOwnBundle_UnauthorisedVendor_400() throws Exception {


        //String requestBody = objectMapper.writeValueAsString(deleteBundleDTO);

        //when(bundleService.getOwnBundles(anyString())).thenReturn(List.of(bundleDTO));


        when(bundleService.getOwnBundles(anyString()))
                .thenThrow(new VendorAuthorizationException());


        mockMvc.perform(get("/bundles/me")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isUnauthorized());
//
        verify(bundleService).getOwnBundles(anyString());



    }



    /**
     * getOwn Bundles test 3
     * internal/ database error
     * 500
     */

    @Test
    public void testUpdateBundle_DBerror_500() throws Exception {

        //String requestBody = objectMapper.writeValueAsString(deleteBundleDTO);

        //when(bundleService.getOwnBundles(anyString())).thenReturn(List.of(bundleDTO));

        doThrow(new RuntimeException("database error"))
                .when(bundleService)
            .getOwnBundles(anyString());

        mockMvc.perform(get("/bundles/me")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isInternalServerError());

        verify(bundleService).getOwnBundles(anyString());


    }
//
//    /**
//     * getVendorBundles test 1
//     * success
//     * 200
//     */
//    @Test
//    public void testGetVendorBundles_Success_200() throws Exception {
//
//        when(bundleService.getVendorBundles(any(UUID.class))).thenReturn(List.of());
//
//
//        mockMvc.perform(get("/bundles/{vendorId}", testVendorId))
//                .andExpect(status().isOk())
//                //ensure the response has the correct bundle id in
//                .andExpect(jsonPath("$[0].vendorId").value(testVendorId.toString()))
//                .andExpect(jsonPath("$[0].bundleId").value(testBundleId.toString()))
//                //ensure the name and price are correct
//                .andExpect(jsonPath("$[0].name").value("Sweet Treat Bundle"))
//                .andExpect(jsonPath("$[0].price").value(2.66));
//
//        verify(bundleService).getVendorBundles(eq(testVendorId));
//    }

    /**
     * getVendorBundles test 2
     * database/internal error
     * 500
     */
    @Test
    public void testGetVendorBundles_DBError_500() throws Exception {

        when(bundleService.getVendorBundles(any(UUID.class)))
                .thenThrow(new RuntimeException("database error"));

        mockMvc.perform(get("/bundles/{vendorId}", testVendorId))

                .andExpect(status().isInternalServerError());

        verify(bundleService).getVendorBundles(eq(testVendorId));
    }


    /**
     * loadSeededData test 1
     * success
     * 204
     */

    @Test
    public void testLoadSeededData_Success_200() throws Exception {

        String requestBody = objectMapper.writeValueAsString(List.of());

//
//
        //
        doNothing().when(bundleService).loadSeededData(anyString(), anyList());

        mockMvc.perform(post("/bundles/internal")
                .header("Authorization", "Bearer internalAccessToken123")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(bundleService).loadSeededData(anyString(), anyList());

    }





    /**
     * loadSeededData test 2
     * unauthorised
     * 401
     */


    @Test
    public void testLoadSeededData_Unauthorised_400() throws Exception {


        String requestBody = objectMapper.writeValueAsString(List.of());

        doThrow(new AuthorizationException())
                .when(bundleService)
                .loadSeededData(anyString(), anyList());

        mockMvc.perform(post("/bundles/internal")
                .header("Authorization", "Bearer internalAccessToken123")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(bundleService).loadSeededData(anyString(), anyList());
    }

    /**
     * loadSeededData test 3
     * internal/database error
     * 500
     */

    @Test
    public void testLoadSeededData_DBError_500() throws Exception {

//        when(bundleService.loadSeededData(anyString(), anyList()))
//            .thenThrow(new RuntimeException("database error"));

        String requestBody = objectMapper.writeValueAsString(List.of());

        doThrow(new RuntimeException("database error"))
                .when(bundleService)
                .loadSeededData(anyString(), anyList());

        mockMvc.perform(post("/bundles/internal")
                .header("Authorization", "Bearer internalAccessToken123")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(bundleService).loadSeededData(anyString(), anyList());
    }














    /**
     * getAllBundlesAvailable test 1
     * success
     * 200
     */
    @Test
    public void testGetAllBundlesAvailable_DefaultParams_200() throws Exception {

        //returns max 50 bundles, offset means it starts from the first bundle, these are the defualt values
        when(bundleService.getAllBundles(50, 0)).thenReturn(List.of());

        mockMvc.perform(get("/bundles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(bundleService).getAllBundles(50, 0);
    }




    /**
     * getAllBundlesAvailable test 2
     * internal/ database error
     * 500
     */
    @Test
    public void testGetAllBundlesAvailable_DBError_500() throws Exception {

        when(bundleService.getAllBundles(50, 0))
                .thenThrow(new RuntimeException("database error"));

        mockMvc.perform(get("/bundles"))
                .andExpect(status().isInternalServerError());

        verify(bundleService).getAllBundles(50, 0);
    }



    /**
     * getDetailedBundle test 1
     * success
     * 200
     */
    @Test
    public void testGetDetailedBundle_Success_200() throws Exception {

        when(bundleService.getDetailedBundle(anyString(), eq(testBundleId)))
                .thenReturn(bundleDTO);

        mockMvc.perform(get("/bundles/detailed/{bundleId}", testBundleId)
                        .header("Authorization", "Bearer userAccessToken123"))
                .andExpect(status().isOk())
                //match the dto from setUp
                .andExpect(jsonPath("$.vendorId").value(testVendorId.toString()))
                .andExpect(jsonPath("$.bundleId").value(testBundleId.toString()))
                .andExpect(jsonPath("$.name").value("Sweet Treat Bundle"))
                .andExpect(jsonPath("$.price").value(2.66));

        verify(bundleService).getDetailedBundle(anyString(), eq(testBundleId));
    }


    /**
     * getDetailedBundle test 2
     * unauthorised (not a valid user token)
     * 401
     */
    @Test
    public void testGetDetailedBundle_Unauthorised_400() throws Exception {

        when(bundleService.getDetailedBundle(anyString(), eq(testBundleId)))

                .thenThrow(new AuthorizationException());

        mockMvc.perform(get("/bundles/detailed/{bundleId}", testBundleId)
                        .header("Authorization", "Bearer userAccessToken123"))
                .andExpect(status().isUnauthorized());

        verify(bundleService).getDetailedBundle(anyString(), eq(testBundleId));
    }

    /**
     * getDetailedBundle test 3
     * bundle not found
     * 404
     */
    @Test
    public void testGetDetailedBundle_NotFound_400() throws Exception {

        when(bundleService.getDetailedBundle(anyString(), eq(testBundleId)))
                .thenThrow(new BundleNotFoundException());

        mockMvc.perform(get("/bundles/detailed/{bundleId}", testBundleId)
                        .header("Authorization", "Bearer userAccessToken123"))
                .andExpect(status().isNotFound());

        verify(bundleService).getDetailedBundle(anyString(), eq(testBundleId));
    }

    /**
     * getDetailedBundle test 4
     * internal/database error
     * 500
     */
    @Test
    public void testGetDetailedBundle_DBError_500() throws Exception {

        when(bundleService.getDetailedBundle(anyString(), eq(testBundleId)))
                .thenThrow(new RuntimeException("database error"));

        mockMvc.perform(get("/bundles/detailed/{bundleId}", testBundleId)

                        .header("Authorization", "Bearer userAccessToken123"))
                .andExpect(status().isInternalServerError());

        verify(bundleService).getDetailedBundle(anyString(), eq(testBundleId));
    }


    /**
     * getBundleMetrics test 1
     * success
     * 200
     */
    @Test
    public void testGetBundleMetrics_Success_200() throws Exception {

     //the period paramete 'week' says what time range to calc stats for
        when(bundleService.getBundleMetrics(anyString(), eq("week")))
                .thenReturn(bundleMetricDTO);

        mockMvc.perform(get("/bundles/metrics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isOk())
                //these match the values from the setUp bundle metrics dto
                .andExpect(jsonPath("$.numCollected").value(23))
                .andExpect(jsonPath("$.numNoShows").value(3))
                .andExpect(jsonPath("$.numExpired").value(6));

        verify(bundleService).getBundleMetrics(anyString(), eq("week"));
    }

    /**
     * getBundleMetrics test 2
     * unauthorised
     * 401
     */
    @Test
    public void testGetBundleMetrics_Unauthorised_400() throws Exception {

        //the period paramete 'week' says what time range to calc stats for
        //this is stats for the week
        when(bundleService.getBundleMetrics(anyString(), eq("week")))
                .thenThrow(new AuthorizationException());

        mockMvc.perform(get("/bundles/metrics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isUnauthorized());

        verify(bundleService).getBundleMetrics(anyString(), eq("week"));
    }


    /**
     * getBundleMetrics test 3
     * internal/database error
     * 500
     */
    @Test
    public void testGetBundleMetrics_DBError_500() throws Exception {

        when(bundleService.getBundleMetrics(anyString(), eq("week")))
                .thenThrow(new RuntimeException("database error"));

        mockMvc.perform(get("/bundles/metrics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isInternalServerError());

        verify(bundleService).getBundleMetrics(anyString(), eq("week"));
    }


    /**
     * getPastBundles test 1
     * success
     * 200
     */
    @Test
    public void testGetPastBundles_Success_200() throws Exception {

        when(bundleService.getPastBundles(anyString(), eq("week")))
                .thenReturn(List.of());

        mockMvc.perform(get("/bundles/analytics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bundleService).getPastBundles(anyString(), eq("week"));
    }


    /**
     * getPastBundles test 2
     * unauthorised
     * 401
     */
    @Test
    public void testGetPastBundles_Unauthorised_400() throws Exception {

        when(bundleService.getPastBundles(anyString(), eq("week")))
                .thenThrow(new AuthorizationException());

        mockMvc.perform(get("/bundles/analytics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isUnauthorized());

        verify(bundleService).getPastBundles(anyString(), eq("week"));
    }

    /**
     * getPastBundles test 3
     * internal/database error
     * 500
     */
    @Test
    public void testGetPastBundles_DBError_500() throws Exception {

        when(bundleService.getPastBundles(anyString(), eq("week")))
                .thenThrow(new RuntimeException("database error"));

        mockMvc.perform(get("/bundles/analytics")
                        .header("Authorization", "Bearer vendorAccessToken123"))
                .andExpect(status().isInternalServerError());

        verify(bundleService).getPastBundles(anyString(), eq("week"));
    }
}
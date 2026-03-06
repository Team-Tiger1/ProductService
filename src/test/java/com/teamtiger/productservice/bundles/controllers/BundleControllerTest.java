package com.teamtiger.productservice.bundles.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtiger.productservice.bundles.entities.BundleCategory;
import com.teamtiger.productservice.bundles.exceptions.VendorAuthorizationException;
import com.teamtiger.productservice.bundles.models.BundleDTO;
import com.teamtiger.productservice.bundles.models.CreateBundleDTO;
import com.teamtiger.productservice.bundles.services.BundleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class BundleControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BundleService bundleService;

    private CreateBundleDTO createBundleDTO;

    private BundleDTO bundleDTO;

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
                .collectionEnd(LocalDateTime.now().plusDays(2))
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
    }


    /**
     * successful vendor bundle creation
     * should return 200
     */

    @Test
    public void testCreateBundle_Success() throws Exception {
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
     * a non-vendor token tries to create a bundle
     * 500 error
    */
    @Test
    public void testCreateBundle_NonVendorRole_Returns500() throws Exception {

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
}
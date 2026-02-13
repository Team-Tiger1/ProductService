package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.exceptions.AllergyNotFoundException;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.ProductSeedDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;
import com.teamtiger.productservice.products.repositories.AllergyRepository;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

//Actual Implementation of ProductService Interface
public class ProductServiceJPA implements ProductService{

    private final ProductRepository productRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AllergyRepository allergyRepository;


    @Override
    //Creates a new product for vendor
    public GetProductDTO createProduct(String accessToken, ProductDTO dto) {

        //Ensures only vendors are allowed to add a Product
        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if (!role.equals("VENDOR")){
            throw new AuthorizationException();
        }


        Set<Allergy> allergySet = allergyRepository.findAllByAllergyTypeIn(dto.allergies());


        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        //Builds Product Entity
        Product product = Product.builder()
                .name(dto.name())
                .retailPrice(dto.retailPrice())
                .weight(dto.weight())
                .vendorId(vendorId)
                .allergies(allergySet)
                .build();

        Product createdProduct = productRepository.save(product);

        //Product is converted into DTO for response
        return ProductMapper.toDTO(createdProduct);
    }


    @Override
    //Gets all products by the Vendor
    public List<GetProductDTO> getVendorProducts(String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        //Gets all products belonging to vendor
        List<Product> productList = productRepository.findAllByVendorId(vendorId);

        //Maps product entities to DTOS
        return productList.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    //Deletes a product owned by the Vendor
    public void deleteProduct(String accessToken, UUID productId) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        //Ensures Product exists
        Product productToBeDeleted = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        //Ensures that products can only be deleted by their respective vendor
        if(!productToBeDeleted.getVendorId().equals(vendorId)){
            throw new AuthorizationException();
        }

        productRepository.delete(productToBeDeleted);


    }

    @Override
    //Patches a vendors product
    public GetProductDTO updateProduct(String accessToken, UUID productId, UpdateProductDTO dto) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product productToBeUpdated = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        //Ensures Product belongs to vendor
        if(!productToBeUpdated.getVendorId().equals(vendorId)){
            throw new RuntimeException("Not the vendor");
        }


        //Each field is updated independently
        if(dto.name()!=null){
            productToBeUpdated.setName(dto.name());
        }

        if (dto.retailPrice() != null) {
            productToBeUpdated.setRetailPrice(dto.retailPrice());
        }

        if(dto.weight()!=null){
            productToBeUpdated.setWeight(dto.weight());
        }

        if(dto.allergies()!=null){

            Set<Allergy> allergySet = productToBeUpdated.getAllergies();


            Set<Allergy> allergyEntities = allergyRepository.findAllByAllergyTypeIn(dto.allergies());

            allergySet.addAll(allergyEntities);

            productToBeUpdated.setAllergies(allergySet);


        }

        Product saved = productRepository.save(productToBeUpdated);
        return ProductMapper.toDTO(saved);

    }


    @Override
    //Loads seeded data
    @Transactional
    public void loadSeededData(String accessToken, List<ProductSeedDTO> products) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        //Only INTERNAL role allowed to load seeded data
        if(!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        //Converts DTOs into Product entities
        List<Product> entities = products.stream()
                .map(dto -> Product.builder()
                        .id(dto.getProductId())
                        .name(dto.getName())
                        .vendorId(dto.getVendorId())
                        .retailPrice(dto.getRetailPrice())
                        .weight(dto.getWeight())
                        .allergies(allergyRepository.findAllByAllergyTypeIn(dto.getAllergies()))
                        .build())
                .toList();

        productRepository.saveAll(entities);



    }
}

package com.teamtiger.productservice.products.services;

import com.teamtiger.productservice.JwtTokenUtil;
import com.teamtiger.productservice.products.entities.Allergy;
import com.teamtiger.productservice.products.entities.AllergyType;
import com.teamtiger.productservice.products.entities.Product;
import com.teamtiger.productservice.products.mappers.ProductMapper;
import com.teamtiger.productservice.products.models.GetProductDTO;
import com.teamtiger.productservice.products.models.ProductDTO;
import com.teamtiger.productservice.products.models.UpdateProductDTO;
import com.teamtiger.productservice.products.repositories.AllergyRepository;
import com.teamtiger.productservice.products.repositories.ProductRepository;
import com.teamtiger.productservice.reservations.exceptions.AuthorizationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceJPA implements ProductService{

    private final ProductRepository productRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AllergyRepository allergyRepository;
    @Override
    public GetProductDTO createProduct(String accessToken, ProductDTO dto) {


        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if (!role.equals("VENDOR")){
            throw new AuthorizationException();
        }
        Set<Allergy> allergySet = new HashSet<>();

        for (AllergyType type : dto.allergies()) {
            Allergy allergy = allergyRepository.findByAllergy(type)
                    .orElseThrow(RuntimeException::new);
            allergySet.add(allergy);
        }


        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product product = Product.builder()
                .name(dto.name())
                .retailPrice(dto.retailPrice())
                .weight(dto.weight())
                .vendorId(vendorId)
                .allergies(allergySet)
                .build();

        Product createdProduct = productRepository.save(product);
        return ProductMapper.toDTO(createdProduct);
    }

    @Override
    public List<GetProductDTO> getVendorProducts(String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        List<Product> productList = productRepository.findAllByVendorId(vendorId);

        return productList.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(String accessToken, UUID productId) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product ProductToBeDeleted = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        productRepository.delete(ProductToBeDeleted);


    }

    @Override
    public GetProductDTO updateProduct(String accessToken, UUID productId, UpdateProductDTO dto) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Product productToBeUpdated = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        if(!productToBeUpdated.getVendorId().equals(vendorId)){
            throw new RuntimeException("Not the vendor");
        }

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


            Set<Allergy> allergyEntities = allergyRepository.findAllByAllergyIn(dto.allergies());

            allergySet.addAll(allergyEntities);

            productToBeUpdated.setAllergies(allergySet);


        }

        Product saved = productRepository.save(productToBeUpdated);
        return ProductMapper.toDTO(saved);

    }


}

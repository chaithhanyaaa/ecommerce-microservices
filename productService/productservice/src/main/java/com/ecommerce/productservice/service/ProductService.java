package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.internal.CreateInventoryRequest;
import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.response.CreateProductResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductSize;
import com.ecommerce.productservice.exception.InvalidPriceException;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ProductSizeRepository;
import com.ecommerce.productservice.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;



import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private  final RestTemplate restTemplate;
    private final ProductSizeRepository productSizeRepository;
    private final HttpServletRequest httpServletRequest;


    public CreateProductResponse createProduct(CreateProductRequest request) {

        if (request.getOfferPrice().compareTo(request.getActualPrice()) > 0) {
            throw new InvalidPriceException("Offer price cannot be greater than actual price.");
        }

        CustomUserPrincipal principal =
                (CustomUserPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        Product product = Product.builder()
                .sellerId(principal.getUserId())
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .category(request.getCategory())
                .gender(request.getGender())
                .actualPrice(request.getActualPrice())
                .offerPrice(request.getOfferPrice())
                .build();

        Product savedProduct = productRepository.save(product);

        // Save available sizes
        List<ProductSize> productSizes = request.getSizeStocks()
                .stream()
                .map(sizeStock -> ProductSize.builder()
                        .productId(savedProduct.getId())
                        .size(sizeStock.getSize())
                        .build())
                .toList();

        productSizeRepository.saveAll(productSizes);

        // Call Inventory Service
        CreateInventoryRequest inventoryRequest =
                CreateInventoryRequest.builder()
                        .productId(savedProduct.getId())
                        .sizeStocks(request.getSizeStocks())
                        .build();

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        if (authorizationHeader == null) {
            throw new IllegalStateException("Authorization header is missing.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);

        HttpEntity<CreateInventoryRequest> entity =
                new HttpEntity<>(inventoryRequest, headers);
        System.out.println("Before Inventory Call");


        restTemplate.postForEntity(
                "http://localhost:8082/internal/inventory",
                entity,
                Void.class
        );
        System.out.println("After Inventory Call");




        return CreateProductResponse.builder()
                .productId(savedProduct.getId())
                .build();
    }

    public ProductResponse getProduct(Long productId)
    {
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        List<String> availableSizes = productSizeRepository
                .findByProductId(productId)
                .stream()
                .map(ProductSize::getSize)
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .category(product.getCategory())
                .gender(product.getGender())
                .actualPrice(product.getActualPrice())
                .offerPrice(product.getOfferPrice())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .availableSizes(availableSizes)
                .build();

    }
}
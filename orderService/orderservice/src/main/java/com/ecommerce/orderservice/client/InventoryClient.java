package com.ecommerce.orderservice.client;


import com.ecommerce.orderservice.dto.request.DeductInventoryRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public void deductInventory(DeductInventoryRequest request) {

        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes())
                        .getRequest();

        String token = currentRequest.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DeductInventoryRequest> entity =
                new HttpEntity<>(request, headers);

        restTemplate.exchange(
                inventoryServiceUrl + "/internal/inventory/deduct",
                HttpMethod.POST,
                entity,
                Void.class
        );
    }
}
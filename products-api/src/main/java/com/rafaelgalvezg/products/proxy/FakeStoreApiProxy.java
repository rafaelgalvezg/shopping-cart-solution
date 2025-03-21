package com.rafaelgalvezg.products.proxy;

import com.rafaelgalvezg.products.dto.ProductDto;
import com.rafaelgalvezg.products.exception.ExternalServiceException;
import com.rafaelgalvezg.products.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeStoreApiProxy {
    private final RestTemplate restTemplate;

    @Value("${fake-store-api.base-url}")
    private String fakeStoreBaseUrl;

    @Value("${fake-store-api.endpoints.products}")
    private String productsEndpoint;

    public ProductDto getProductById(Long productId) {
        String url = fakeStoreBaseUrl + productsEndpoint + "/" + productId;
        log.info("Fetching product ID: {} from FakeStoreAPI: {}", productId, url);

        try {
            ProductDto product = restTemplate.getForObject(url, ProductDto.class);
            if (product == null) {
                log.error("Product ID: {} not found in FakeStoreAPI (null response)", productId);
                throw new ResourceNotFoundException("Product not found with ID: " + productId);
            }
            log.debug("Product fetched successfully: {}", product);
            return product;
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Product ID: {} not found in FakeStoreAPI", productId);
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        } catch (HttpClientErrorException ex) {
            log.error("Client error while fetching product ID: {} from FakeStoreAPI: {}", productId, ex.getMessage());
            throw new ExternalServiceException("Invalid request to external API: " + ex.getMessage(), ex);
        } catch (HttpServerErrorException ex) {
            log.error("Server error while fetching product ID: {} from FakeStoreAPI: {}", productId, ex.getMessage());
            throw new ExternalServiceException("External API server error: " + ex.getMessage(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to FakeStoreAPI for product ID: {}: {}", productId, ex.getMessage());
            throw new ExternalServiceException("External API is unavailable: " + ex.getMessage(), ex);
        }
    }

    public List<ProductDto> getAllProducts() {
        String url = fakeStoreBaseUrl + productsEndpoint;
        log.info("Fetching all products from FakeStoreAPI: {}", url);

        try {
            ProductDto[] productsArray = restTemplate.getForObject(url, ProductDto[].class);
            if (productsArray == null || productsArray.length == 0) {
                log.warn("No products found in FakeStoreAPI");
                return Collections.emptyList();
            }
            List<ProductDto> products = Arrays.asList(productsArray);
            log.debug("Fetched {} products successfully", products.size());
            return products;
        } catch (HttpClientErrorException ex) {
            log.error("Client error while fetching all products from FakeStoreAPI: {}", ex.getMessage());
            throw new ExternalServiceException("Invalid request to external API: " + ex.getMessage(), ex);
        } catch (HttpServerErrorException ex) {
            log.error("Server error while fetching all products from FakeStoreAPI: {}", ex.getMessage());
            throw new ExternalServiceException("External API server error: " + ex.getMessage(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to FakeStoreAPI for all products: {}", ex.getMessage());
            throw new ExternalServiceException("External API is unavailable: " + ex.getMessage(), ex);
        }
    }
}
package com.rafaelgalvezg.products.service.impl;

import com.rafaelgalvezg.products.dto.ProductDto;
import com.rafaelgalvezg.products.proxy.FakeStoreApiProxy;
import com.rafaelgalvezg.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final FakeStoreApiProxy proxy;

    @Override
    public List<ProductDto> getAllProducts() {
        return proxy.getAllProducts();
    }

    @Override
    public ProductDto getProductById(long id) {
        return proxy.getProductById(id);
    }
}
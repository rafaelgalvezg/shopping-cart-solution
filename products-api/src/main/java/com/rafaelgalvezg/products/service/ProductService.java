package com.rafaelgalvezg.products.service;

import com.rafaelgalvezg.products.dto.ProductDto;
import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(long id);
}
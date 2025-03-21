package com.rafaelgalvezg.products.dto;

public record ProductDto(
        long id,
        String title,
        double price,
        String description,
        String category,
        String image,
        RatingDto rating
) {}
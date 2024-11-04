package com.musinsa.assignment.repository;

import com.musinsa.assignment.domain.dto.BrandTotalPriceDto;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;

import java.util.List;
import java.util.Optional;

public interface ItemSortRepository {
    List<Item> findLowestPriceItemPerCategory();
    List<Item> findHighestPriceItemPerCategory();
    Optional<Item> findLowestPriceItemByCategory(Category category);
    Optional<Item> findHighestPriceItemByCategory(Category category);
    List<BrandTotalPriceDto> findBrandWithLowestTotalPrice();
    List<Item> findItemsByBrand(Brand brand);
}

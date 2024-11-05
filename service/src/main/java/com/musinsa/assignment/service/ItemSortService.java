package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.BrandTotalPriceDto;
import com.musinsa.assignment.domain.dto.CategoryAndPriceDto;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import com.musinsa.assignment.repository.ItemSortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * item 정렬 조건별 결과 unmodifiable list로 반환
 */
@Service
@RequiredArgsConstructor
public class ItemSortService {

    private final ItemSortRepository itemSortRepository;

    List<ItemDto> lowestPriceItemPerCategory() {
        return itemSortRepository.findLowestPriceItemPerCategory()
                .stream().map(ItemDto::new)
                .toList();

    }
    List<ItemDto> highestPriceItemPerCategory() {
        return itemSortRepository.findHighestPriceItemPerCategory()
                .stream().map(ItemDto::new)
                .toList();
    }

    ItemDto lowestPriceItemByCategory(Category category) {
        return new ItemDto(
                itemSortRepository.findLowestPriceItemByCategory(category)
                        .orElseThrow(() -> new NeedMonitorException("lowestPriceItemByCategory", category.getCategoryName() + " 데이터 없음") )
        );
    }

    ItemDto highestPriceItemByCategory(Category category) {
        return new ItemDto(
                itemSortRepository.findHighestPriceItemByCategory(category).orElseThrow()
        );
    }

    /**
     * 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는 브랜드와 카테고리의 상품가격, 총액 조회
     */
    public LowestTotalBrand lowestTotalBrand() {
        // 브랜드별 총액이 가장 낮은 브랜드 조회
        List<BrandTotalPriceDto> brandTotalPrices = itemSortRepository.findBrandWithLowestTotalPrice();
        if (brandTotalPrices.isEmpty()) {
            return null;
        }
        BrandTotalPriceDto brandTotalPrice = brandTotalPrices.get(0);

        // 브랜드별 상품 조회
        List<Item> items = itemSortRepository.findItemsByBrand(brandTotalPrice.getBrand());
        return new LowestTotalBrand(
                brandTotalPrice.getBrand().getBrandName(),
                items.stream().map(it -> new CategoryAndPriceDto(it.getCategoryName(), it.getPrice())).toList(),
                brandTotalPrice.getTotalPrice()
        );
    }
}

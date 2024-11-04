package com.musinsa.assignment.controller;

import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.service.ResponseCachingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandItemController {

    private final ResponseCachingService responseCachingService;

    /**
     * 구현2) - 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는 브랜드와 카테고리의 상품가격, 총액을 조회하는 API
     */
    @GetMapping("/lowest-total-price/items")
    public LowestTotalBrand lowestTotalBrand() {
        return responseCachingService.getLowestTotalBrand();
    }

}

package com.musinsa.assignment.controller;

import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.exhandle.exception.UserException;
import com.musinsa.assignment.service.CategoryService;
import com.musinsa.assignment.service.RedisQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryItemController {

    private final RedisQueryService responseCachingService;
    private final CategoryService categoryService;

    /**
     * 구현 1) - 카테고리 별 최저가격 브랜드와 상품 가격, 총액을 조회하는 API
     */
    @GetMapping("/lowest-price-items")
    public LowestPriceItemPerCategory lowestPriceItemPerCategory() {
        return responseCachingService.getLowestPriceItemPerCategory();
    }

    /**
     * 구현 3) - 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격을 조회하는 API
     * @param category 카테고리 한글 이름
     */
    @GetMapping("/{category}/extreme-price-items")
    public ExtremePriceItemByCategory extremePriceItemsPerCategory(@PathVariable String category) {
        if (!categoryService.isExistCategory(category)) {
            throw new UserException("존재하지 않는 카테고리입니다.", "입력: " + category);
        }
        return responseCachingService.getExtremePriceItemPerCategory(category);
    }
}

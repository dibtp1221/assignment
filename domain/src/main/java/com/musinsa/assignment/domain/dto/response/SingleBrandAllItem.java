package com.musinsa.assignment.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.musinsa.assignment.domain.dto.CategoryAndPriceDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class SingleBrandAllItem {
    @JsonProperty("브랜드")
    private String brand;
    @JsonProperty("카테고리")
    private List<CategoryAndPriceDto> categoryAndPriceDtos;
    @JsonProperty("총액")
    private BigDecimal price;
    @JsonProperty("총액")
    public String getFormattedPrice() {
        return String.format("%,d", price.intValue());
    }

    public SingleBrandAllItem() {
    }

    public SingleBrandAllItem(String brand, List<CategoryAndPriceDto> categoryAndPriceDtos, BigDecimal price) {
        this.brand = brand;
        this.categoryAndPriceDtos = categoryAndPriceDtos;
        this.price = price;
    }
}
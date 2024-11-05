package com.musinsa.assignment.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategoryAndPriceDto {
    @JsonProperty("카테고리")
    private String category;
    @JsonProperty("가격")
    private BigDecimal price;
    @JsonProperty("가격")
    public String getFormattedPrice() {
        return String.format("%,d", price.intValue());
    }

    public CategoryAndPriceDto() {
    }

    public CategoryAndPriceDto(String category, BigDecimal price) {
        this.category = category;
        this.price = price;
    }

}

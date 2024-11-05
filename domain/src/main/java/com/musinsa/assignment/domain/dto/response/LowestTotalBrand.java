package com.musinsa.assignment.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.musinsa.assignment.domain.dto.CategoryAndPriceDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class LowestTotalBrand {

    @JsonProperty("최저가")
    private SingleBrandAllItem singleBrandAllItems;

    public LowestTotalBrand() {
    }

    public LowestTotalBrand(String brand, List<CategoryAndPriceDto> categoryAndPriceDtos, BigDecimal price) {
        this.singleBrandAllItems = new SingleBrandAllItem(brand, categoryAndPriceDtos, price);
    }

    @JsonIgnore
    public String getBrandName() {
        return singleBrandAllItems.getBrand();
    }

    @JsonIgnore
    public BigDecimal getTotalPrice() {
        return singleBrandAllItems.getPrice();
    }
}

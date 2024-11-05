package com.musinsa.assignment.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.musinsa.assignment.domain.dto.BrandAndPriceDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ExtremePriceItemByCategory {
    @JsonProperty("카테고리")
    private String categoryName;
    @Setter
    @JsonProperty("최저가")
    private List<BrandAndPriceDto> lowestItem;
    @Setter
    @JsonProperty("최고가")
    private List<BrandAndPriceDto> highestItem;

    public ExtremePriceItemByCategory() {
    }

    public ExtremePriceItemByCategory(String categoryName, List<BrandAndPriceDto> lowestItem, List<BrandAndPriceDto> highestItem) {
        this.categoryName = categoryName;
        this.lowestItem = lowestItem;
        this.highestItem = highestItem;
    }

    @JsonIgnore
    public BigDecimal getLowestPrice() {
        return lowestItem.get(0).getPrice();
    }

    @JsonIgnore
    public BigDecimal getHighestPrice() {
        return highestItem.get(0).getPrice();
    }

    @JsonIgnore
    public String getLowestBrand() {
        return lowestItem.get(0).getBrand();
    }

    @JsonIgnore
    public String getHighestBrand() {
        return highestItem.get(0).getBrand();
    }
}

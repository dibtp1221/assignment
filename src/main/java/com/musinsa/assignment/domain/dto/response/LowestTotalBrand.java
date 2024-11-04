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
    private final SingleBrandAllItem singleBrandAllItems;

    public LowestTotalBrand(String brand, List<CategoryAndPriceDto> categoryAndPriceDtos, BigDecimal price) {
        this.singleBrandAllItems = new SingleBrandAllItem(brand, categoryAndPriceDtos, price);
    }

    @Getter
    private static class SingleBrandAllItem {
        @JsonProperty("브랜드")
        private final String brand;
        @JsonProperty("카테고리")
        private final List<CategoryAndPriceDto> categoryAndPriceDtos;
        @JsonProperty("총액")
        private final BigDecimal price;
        @JsonProperty("총액")
        public String getFormattedPrice() {
            return String.format("%,d", price.intValue());
        }

        public SingleBrandAllItem(String brand, List<CategoryAndPriceDto> categoryAndPriceDtos, BigDecimal price) {
            this.brand = brand;
            this.categoryAndPriceDtos = categoryAndPriceDtos;
            this.price = price;
        }
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

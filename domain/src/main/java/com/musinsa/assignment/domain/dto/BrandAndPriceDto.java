package com.musinsa.assignment.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class BrandAndPriceDto {
    @JsonProperty("브랜드")
    private String brand;
    @JsonProperty("가격")
    private BigDecimal price;
    @JsonProperty("가격")
    public String getFormattedPrice() {
        return String.format("%,d", price.intValue());
    }

    public BrandAndPriceDto() {
    }

    public BrandAndPriceDto(String brand, int price) {
        this.brand = brand;
        this.price = BigDecimal.valueOf(price);
    }
    public BrandAndPriceDto(String brand, BigDecimal price) {
        this.brand = brand;
        this.price = price;
    }

    @Override
    public String toString() {
        return "BrandAndPriceDto{" +
                "brand='" + brand + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrandAndPriceDto that = (BrandAndPriceDto) o;
        return Objects.equals(brand, that.brand) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brand, price);
    }
}

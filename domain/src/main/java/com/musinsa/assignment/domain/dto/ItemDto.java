package com.musinsa.assignment.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.musinsa.assignment.domain.entity.Item;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class ItemDto {
    @JsonProperty("category")
    private String categoryName;
    @JsonProperty("brand")
    private String brandName;
    private BigDecimal price;

    public ItemDto() {
    }

    public ItemDto(String categoryName, String brandName, int price) {
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.price = BigDecimal.valueOf(price);
    }

    public ItemDto(Item item) {
        this.categoryName = item.getCategory().getCategoryName();
        this.brandName = item.getBrand().getBrandName();
        this.price = item.getPrice();
    }

    @Override
    public String toString() {
        return "ItemDto{" +
                "category='" + categoryName + '\'' +
                ", brand='" + brandName + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDto itemDto = (ItemDto) o;
        return Objects.equals(categoryName, itemDto.categoryName) && Objects.equals(brandName, itemDto.brandName) && Objects.equals(price, itemDto.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, brandName, price);
    }
}

package com.musinsa.assignment.domain.dto;

import com.musinsa.assignment.domain.entity.Item;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemMessageDto {

    private Long categoryId;
    private Long brandId;
    private BigDecimal price;

    public ItemMessageDto() {
    }

    public ItemMessageDto(Long categoryId, Long brandId, BigDecimal price) {
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.price = price;
    }

    public static ItemMessageDto from(Item item) {
        return new ItemMessageDto(item.getCategory().getId(), item.getBrand().getId(), item.getPrice());
    }
}

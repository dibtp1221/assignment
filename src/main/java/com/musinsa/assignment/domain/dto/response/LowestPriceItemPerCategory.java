package com.musinsa.assignment.domain.dto.response;

import com.musinsa.assignment.domain.dto.ItemDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class LowestPriceItemPerCategory {

    private final List<ItemDto> items;
    private final BigDecimal totalPrice;

    public LowestPriceItemPerCategory(List<ItemDto> items) {
        this.items = items;
        this.totalPrice = items.stream().map(ItemDto::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

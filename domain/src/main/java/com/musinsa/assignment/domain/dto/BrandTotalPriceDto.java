package com.musinsa.assignment.domain.dto;

import com.musinsa.assignment.domain.entity.Brand;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BrandTotalPriceDto {
    private final Brand brand;
    private final BigDecimal totalPrice;

    public BrandTotalPriceDto(Brand brand, BigDecimal totalPrice) {
        this.brand = brand;
        this.totalPrice = totalPrice;
    }
}

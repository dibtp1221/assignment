package com.musinsa.assignment.controller;

import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.inner.ResultDto;
import com.musinsa.assignment.domain.dto.request.PriceDto;
import com.musinsa.assignment.domain.dto.response.ResponseDto;
import com.musinsa.assignment.service.ItemUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemUpdateController {

    private final ItemUpdateService itemUpdateService;

    @PatchMapping("/price")
    public ResponseDto<ItemDto> updateItem(@RequestBody ItemDto itemDto) {
        ResultDto<ItemDto> result = itemUpdateService.updateItemPrice(itemDto);
        return new ResponseDto<>(result.success(), result.message(), result.result());
    }

    @PatchMapping("/{itemId}/price")
    public ResponseDto<ItemDto> updateItem(@PathVariable String itemId, @RequestBody PriceDto priceDto) {
        ResultDto<ItemDto> result = itemUpdateService.updateItemPrice(itemId, BigDecimal.valueOf(priceDto.getPrice()));
        return new ResponseDto<>(result.success(), result.message(), result.result());
    }

}

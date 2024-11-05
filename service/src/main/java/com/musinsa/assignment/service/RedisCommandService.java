package com.musinsa.assignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.musinsa.assignment.domain.dto.BrandAndPriceDto;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class RedisCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper
            = JsonMapper.builder()
            .disable(com.fasterxml.jackson.databind.MapperFeature.USE_ANNOTATIONS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public RedisCommandService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 카테고리별 최저가 상품 정보 캐시 업데이트
     * @param items 각 카테고리별 최저가 상품들
     */
    public void setLowestPriceItems(List<ItemDto> items) {
        try {
            redisTemplate.opsForValue().set("lowestPriceItemPerCategory", objectMapper.writeValueAsString(new LowestPriceItemPerCategory(items)));
        } catch (JsonProcessingException e) {
            throw new NeedMonitorException("setLowestPriceItems", "캐시 업데이트 실패");
        }
    }

    public void updateExtremePriceItems(List<ItemDto> lowestPriceItems, List<ItemDto> highestPriceItems) {
        for (int i = 0; i < lowestPriceItems.size(); i++) {
            ItemDto lowest = lowestPriceItems.get(i);
            ItemDto highest = highestPriceItems.get(i);
            try {
                String jsonString = objectMapper.writeValueAsString(
                        ExtremePriceItemByCategory.builder()
                                .categoryName(lowest.getCategoryName())
                                .lowestItem(Collections.singletonList(new BrandAndPriceDto(lowest.getBrandName(), lowest.getPrice())))
                                .highestItem(Collections.singletonList(new BrandAndPriceDto(highest.getBrandName(), highest.getPrice())))
                                .build()
                );
                redisTemplate.opsForHash().put(
                        "extremePriceItemPerCategory",
                        lowest.getCategoryName(),
                        jsonString
                );
            } catch (Exception e) {
                throw new NeedMonitorException("updateExtremePriceItems", "직렬화 실패로 캐시 업데이트 실패");
            }
        }
    }

    public void setLowestTotalBrand(LowestTotalBrand lowestTotalBrand) {
        try {
            redisTemplate.opsForValue().set("lowestTotalBrand", objectMapper.writeValueAsString(lowestTotalBrand));
        } catch (JsonProcessingException e) {
            throw new NeedMonitorException("setLowestTotalBrand", "캐시 업데이트 실패");
        }
    }

    /**
     * 최저가, 최고가 캐시에 특정 카테고리 최고가 상품 정보 변경
     */
    public void changeExtreme(String categoryName, ExtremePriceItemByCategory extremePriceItemPerCategory) {
        try {
            String s = objectMapper.writeValueAsString(extremePriceItemPerCategory);
            redisTemplate.opsForHash().put(
                    "extremePriceItemPerCategory",
                    categoryName,
                    s
            );
        } catch (JsonProcessingException e) {
            throw new NeedMonitorException("changeExtreme", "직렬화 실패로 캐시 업데이트 실패");
        }
    }
}

package com.musinsa.assignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.domain.entity.Item;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisQueryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper
            = JsonMapper.builder()
            .disable(com.fasterxml.jackson.databind.MapperFeature.USE_ANNOTATIONS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public RedisQueryService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 최저가 캐시 변경 필요 여부 확인
     * @param item 변경 요청 온 상품
     * @return 최저가 캐시 변경 필요 여부
     */
    public boolean needToUpdateLowestPriceItemByCategory(Item item) {
        ExtremePriceItemByCategory cache = getExtremePriceItemPerCategory(item.getCategoryName());
        return item.getBrandName().equals(cache.getLowestBrand())
                || item.getPrice().compareTo(cache.getLowestPrice()) < 0;
    }

    /**
     * 최고가 캐시 변경 필요 여부 확인
     * @param item 변경 요청 온 상품
     * @return 최고가 캐시 변경 필요 여부
     */
    public boolean needToUpdateHighestPriceItemByCategory(Item item) {
        ExtremePriceItemByCategory cache = getExtremePriceItemPerCategory(item.getCategoryName());
        return item.getBrandName().equals(cache.getHighestBrand())
                || item.getPrice().compareTo(cache.getHighestPrice()) > 0;
    }


    public boolean categoryNotExistsInExtremeCache(String categoryName) {
        return !redisTemplate.opsForHash().hasKey("extremePriceItemPerCategory", categoryName);
    }
    /**
     * 캐싱된 카테고리별 최저가, 최고가 정보 반환
     */
    public ExtremePriceItemByCategory getExtremePriceItemPerCategory(String categoryName) {
        String s = (String) redisTemplate.opsForHash().get("extremePriceItemPerCategory", categoryName);
        try {
            return objectMapper.readValue(
                    s,
                    ExtremePriceItemByCategory.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public LowestPriceItemPerCategory getLowestPriceItemPerCategory() {
        try {
            return objectMapper.readValue(
                    redisTemplate.opsForValue().get("lowestPriceItemPerCategory"),
                    LowestPriceItemPerCategory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 캐싱된 브랜드별 총액 최저가 정보 반환
     */
    public LowestTotalBrand getLowestTotalBrand() {
        try {
            return objectMapper.readValue(
                    redisTemplate.opsForValue().get("lowestTotalBrand"),
                    LowestTotalBrand.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean categoryNotExistsInLowestCache(String categoryName) {
        return getLowestPriceItemPerCategory().getItems().stream()
                .noneMatch(it -> it.getCategoryName().equals(categoryName));
    }

}

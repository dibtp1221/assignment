package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.BrandAndPriceDto;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.ItemMessageDto;
import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import com.musinsa.assignment.repository.BrandJpaRepository;
import com.musinsa.assignment.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResponseCachingService {

    private final ItemSortService itemSortService;
    private final RedisTemplate<String, String> redisTemplate;
    private final CategoryJpaRepository categoryJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    private final RedisQueryService redisQueryService;
    private final RedisCommandService redisCommandService;

    /**
     * 전체 캐시 업데이트 메시지 들어오면 업데이트 필요 여부 확인 -> 업데이트 or 무시
     */
    public void updateAllCache(Long messageVersion) {
        String tempVersion = redisTemplate.opsForValue().get("consumer:update:version");
        long consumerVersion = (tempVersion == null) ? 0 : Long.parseLong(tempVersion);
        if (consumerVersion >= messageVersion) {
            return;
        }
        String producerVersion = redisTemplate.opsForValue().get("producer:update:version");
        if (producerVersion == null) {
            throw new NeedMonitorException("updateAllCache", "producer:update:version 키 없음");
        }
        redisTemplate.opsForValue().set("consumer:update:version", producerVersion);
        updateCache();
    }

    public void updateSingleItem(ItemMessageDto itemMessageDto) {
        updateCacheSingleItem(new Item(
                itemMessageDto.getPrice(),
                brandJpaRepository.findById(itemMessageDto.getBrandId()).orElseThrow(),
                categoryJpaRepository.findById(itemMessageDto.getCategoryId()).orElseThrow()
        ));
    }

    /**
     * 전체 캐시 업데이트
     * 수정 요청이 동시다발적으로 들어올 때 가장 마지막의 업데이트 되기 전 db 데이터를 읽은 스레드가
     * 제일 마지막으로 캐시 갱신하게 되는 경우 막기 위해 synchronized 처리.
     */
    public synchronized void updateCache() {
        List<ItemDto> lowestPriceItems = itemSortService.lowestPriceItemPerCategory();
        List<ItemDto> highestPriceItems = itemSortService.highestPriceItemPerCategory();

        if (lowestPriceItems.size() != highestPriceItems.size()) {
            throw new NeedMonitorException("updateCache", "카테고리별 최저가, 최고가 상품 개수 불일치");
        }

        redisCommandService.setLowestPriceItems(lowestPriceItems);

        redisCommandService.updateExtremePriceItems(lowestPriceItems, highestPriceItems);

        updateLowestTotalBrand();
    }

    /**
     * 상품 하나 가격 바뀌었을 때 -> 구현 1번 해당 카테고리, 구현 2번 전체, 구현 3번 해당 카테고리
     */
    public synchronized void updateCacheSingleItem(Item item) {

        // 카테고리별 최저,최고가 캐시 없으면 전체 캐시 업데이트
        if (redisQueryService.categoryNotExistsInExtremeCache(item.getCategoryName())) {
            updateCache();
            return;
        }

        // 요청된 상품 가격 변경 -> 최저가 관련 API 응답 변경되어야 하는지 확인
        if (redisQueryService.needToUpdateLowestPriceItemByCategory(item)) {
            updateLowestSingleCategory(item.getCategory());
        }

        // 요청된 상품 가격 변경 -> 최고가 관련 API 응답 변경되어야 하는지 확인
        if (redisQueryService.needToUpdateHighestPriceItemByCategory(item)) {
            updateHighestSingleCategory(item.getCategory());
        }

        updateLowestTotalBrand();
    }

    /**
     * 단일 브랜드 & 모든 카테고리 상품 총액 최저가 정보 캐싱
     */
    private void updateLowestTotalBrand() {
        redisCommandService.setLowestTotalBrand(itemSortService.lowestTotalBrand());
    }

    /**
     * 단일 카테고리 최고가 정보 캐싱
     */
    private void updateHighestSingleCategory(Category category) {
        ItemDto newItemDto = itemSortService.highestPriceItemByCategory(category);
        changeHighestItemOfExtreme(category.getCategoryName(), newItemDto.getBrandName(), newItemDto.getPrice());
    }

    /**
     * 단일 카테고리 최저가 정보 캐싱
     */
    private void updateLowestSingleCategory(Category category) {
        if (redisQueryService.categoryNotExistsInLowestCache(category.getCategoryName())) {
            throw new NeedMonitorException("updateLowestSingleCategory", "최저가 캐시에 카테고리 없음 " + category.getCategoryName());
        }

        ItemDto newItemDto = itemSortService.lowestPriceItemByCategory(category);
        changeSingleCategoryLowestItem(category.getCategoryName(), newItemDto);
        changeLowestItemOfExtreme(category.getCategoryName(), newItemDto.getBrandName(), newItemDto.getPrice());
    }

    /**
     * 특정 카테고리 최저가 캐시 변경
     * @param categoryName 캐싱 필요한 카테고리명
     * @param newItemDto 최저가 상품 정보
     */
    private void changeSingleCategoryLowestItem(String categoryName, ItemDto newItemDto) {

        LowestPriceItemPerCategory lowestPriceItemPerCategory = redisQueryService.getLowestPriceItemPerCategory();
        List<ItemDto> list = lowestPriceItemPerCategory.getItems().stream().map(
                it -> it.getCategoryName().equals(categoryName) ? newItemDto : it
        ).toList();
        redisCommandService.setLowestPriceItems(list);
    }

    /**
     * 최저가, 최고가 캐시에 특정 카테고리 최저가 상품 정보 변경
     * @param categoryName 카테고리명
     * @param brandName 변경될 상품의 브랜드명
     * @param price 변경될 상품의 가격
     */
    private void changeLowestItemOfExtreme(String categoryName, String brandName, BigDecimal price) {
        ExtremePriceItemByCategory extremePriceItemPerCategory = redisQueryService.getExtremePriceItemPerCategory(categoryName);
        extremePriceItemPerCategory.setLowestItem(Collections.singletonList(new BrandAndPriceDto(brandName, price)));
        redisCommandService.changeExtreme(categoryName, extremePriceItemPerCategory);
    }

    /**
     * 최저가, 최고가 캐시에 특정 카테고리 최저가 상품 정보 변경
     * @param categoryName 카테고리명
     * @param brandName 변경될 상품의 브랜드명
     * @param price 변경될 상품의 가격
     */
    private void changeHighestItemOfExtreme(String categoryName, String brandName, BigDecimal price) {
        ExtremePriceItemByCategory extremePriceItemPerCategory = redisQueryService.getExtremePriceItemPerCategory(categoryName);
        extremePriceItemPerCategory.setHighestItem(Collections.singletonList(new BrandAndPriceDto(brandName, price)));
        redisCommandService.changeExtreme(categoryName, extremePriceItemPerCategory);
    }
}

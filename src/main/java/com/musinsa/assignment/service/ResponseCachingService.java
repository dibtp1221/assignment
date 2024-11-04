package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.BrandAndPriceDto;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ResponseCachingService {

    private final ItemSortService itemSortService;

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

        ResponseCache.setLowestPriceItems(lowestPriceItems);

        ResponseCache.updateExtremePriceItems(lowestPriceItems, highestPriceItems);

        updateLowestTotalBrand();
    }

    /**
     * 상품 하나 가격 바뀌었을 때 -> 구현 1번 해당 카테고리, 구현 2번 전체, 구현 3번 해당 카테고리
     */
    public synchronized void updateCacheSingleItem(Item item) {

        // 카테고리별 최저,최고가 캐시 없으면 전체 캐시 업데이트
        if (ResponseCache.categoryNotExistsInExtremeCache(item.getCategoryName())) {
            updateCache();
            return;
        }

        // 요청된 상품 가격 변경 -> 최저가 관련 API 응답 변경되어야 하는지 확인
        if (ResponseCache.needToUpdateLowestPriceItemByCategory(item)) {
            updateLowestSingleCategory(item.getCategory());
        }

        // 요청된 상품 가격 변경 -> 최고가 관련 API 응답 변경되어야 하는지 확인
        if (ResponseCache.needToUpdateHighestPriceItemByCategory(item)) {
            updateHighestSingleCategory(item.getCategory());
        }

        updateLowestTotalBrand();

    }

    /**
     * 캐싱된 카테고리별 최저가 정보 반환
     */
    public LowestPriceItemPerCategory getLowestPriceItemPerCategory() {
        return ResponseCache.lowestPriceItemPerCategory;
    }

    /**
     * 캐싱된 카테고리별 최저가, 최고가 정보 반환
     */
    public ExtremePriceItemByCategory getExtremePriceItemsByCategory(String category) {
        if (!ResponseCache.extremePriceItemPerCategory.containsKey(category)) {
            throw new NeedMonitorException("getExtremePriceItemsByCategory", "카테고리 캐시 없음: " + category);
        }
        return ResponseCache.extremePriceItemPerCategory.get(category);
    }

    /**
     * 캐싱된 브랜드별 총액 최저가 정보 반환
     */
    public LowestTotalBrand getLowestTotalBrand() {
        return ResponseCache.lowestTotalBrand;
    }


    /**
     * 단일 카테고리 최저가 정보 캐싱
     */
    private void updateLowestSingleCategory(Category category) {
        if (ResponseCache.categoryNotExistsInLowestCache(category.getCategoryName())) {
            throw new NeedMonitorException("updateLowestSingleCategory", "최저가 캐시에 카테고리 없음 " + category.getCategoryName());
        }

        ItemDto newItemDto = itemSortService.lowestPriceItemByCategory(category);
        ResponseCache.changeSingleCategoryLowestItem(category.getCategoryName(), newItemDto);
        ResponseCache.changeLowestItemOfExtreme(category.getCategoryName(), newItemDto.getBrandName(), newItemDto.getPrice());
    }

    /**
     * 단일 카테고리 최고가 정보 캐싱
     */
    private void updateHighestSingleCategory(Category category) {
        ItemDto newItemDto = itemSortService.highestPriceItemByCategory(category);
        ResponseCache.changeHighestItemOfExtreme(category.getCategoryName(), newItemDto.getBrandName(), newItemDto.getPrice());
    }

    /**
     * 단일 브랜드 & 모든 카테고리 상품 총액 최저가 정보 캐싱
     */
    private void updateLowestTotalBrand() {
        ResponseCache.lowestTotalBrand = itemSortService.lowestTotalBrand();
    }

    /**
     * 캐시 관리 클래스
     * API 응답 자체를 캐싱
     */
    @Getter
    private static class ResponseCache {
        // 각 카테고리별 최저가 상품 정보 - 구현 1번
        private static LowestPriceItemPerCategory lowestPriceItemPerCategory;
        // 각 카테고리별 최저가, 최고가 상품 정보 - 구현 3번
        private static final Map<String, ExtremePriceItemByCategory> extremePriceItemPerCategory
                = new ConcurrentHashMap<>();
        // 브랜드별 총액 최저가 상품 정보 - 구현 2번
        private static LowestTotalBrand lowestTotalBrand;

        /**
         * 최저가 캐시 변경 필요 여부 확인
         * @param item 변경 요청 온 상품
         * @return 최저가 캐시 변경 필요 여부
         */
        private static boolean needToUpdateLowestPriceItemByCategory(Item item) {
            ExtremePriceItemByCategory cache = extremePriceItemPerCategory.get(item.getCategoryName());
            return item.getBrandName().equals(cache.getLowestBrand())
                    || item.getPrice().compareTo(cache.getLowestPrice()) < 0;
        }

        /**
         * 최고가 캐시 변경 필요 여부 확인
         * @param item 변경 요청 온 상품
         * @return 최고가 캐시 변경 필요 여부
         */
        private static boolean needToUpdateHighestPriceItemByCategory(Item item) {
            ExtremePriceItemByCategory cache = extremePriceItemPerCategory.get(item.getCategoryName());
            return item.getBrandName().equals(cache.getHighestBrand())
                    || item.getPrice().compareTo(cache.getHighestPrice()) > 0;
        }

        /**
         * 특정 카테고리 최저가 캐시 변경
         * @param categoryName 캐싱 필요한 카테고리명
         * @param newItemDto 최저가 상품 정보
         */
        private static void changeSingleCategoryLowestItem(
                String categoryName, ItemDto newItemDto
        ) {
            List<ItemDto> list = lowestPriceItemPerCategory.getItems().stream().map(
                    it -> it.getCategoryName().equals(categoryName) ? newItemDto : it
            ).toList();
            lowestPriceItemPerCategory = new LowestPriceItemPerCategory(list);
        }

        /**
         * 카테고리별 최저가 상품 정보 캐시 업데이트
         * @param items 각 카테고리별 최저가 상품들
         */
        private static void setLowestPriceItems(List<ItemDto> items) {
            lowestPriceItemPerCategory = new LowestPriceItemPerCategory(items);
        }

        private static boolean categoryNotExistsInExtremeCache(String categoryName) {
            return !extremePriceItemPerCategory.containsKey(categoryName);
        }

        private static boolean categoryNotExistsInLowestCache(String categoryName) {
            return lowestPriceItemPerCategory.getItems().stream()
                    .noneMatch(it -> it.getCategoryName().equals(categoryName));
        }

        /**
         * 최저가, 최고가 캐시에 특정 카테고리 최저가 상품 정보 변경
         * @param categoryName 카테고리명
         * @param brandName 변경될 상품의 브랜드명
         * @param price 변경될 상품의 가격
         */
        private static void changeLowestItemOfExtreme(String categoryName, String brandName, BigDecimal price) {
            extremePriceItemPerCategory.get(categoryName)
                    .setLowestItem(Collections.singletonList(new BrandAndPriceDto(brandName, price)));
        }

        /**
         * 최저가, 최고가 캐시에 특정 카테고리 최고가 상품 정보 변경
         * @param categoryName 카테고리명
         * @param brandName 변경될 상품의 브랜드명
         * @param price 변경될 상품의 가격
         */
        private static void changeHighestItemOfExtreme(String categoryName, String brandName, BigDecimal price) {
            extremePriceItemPerCategory.get(categoryName)
                    .setHighestItem(Collections.singletonList(new BrandAndPriceDto(brandName, price)));
        }

        /**
         * 각 카테고리별 최저가, 최고가 상품 정보 캐시 업데이트
         * @param lowestPriceItems 최저가 상품 리스트
         * @param highestPriceItems 최고가 상품 리스트
         */
        private static void updateExtremePriceItems(List<ItemDto> lowestPriceItems, List<ItemDto> highestPriceItems) {
            ResponseCache.extremePriceItemPerCategory.clear();

            for (int i = 0; i < lowestPriceItems.size(); i++) {
                ItemDto lowest = lowestPriceItems.get(i);
                ItemDto highest = highestPriceItems.get(i);
                ResponseCache.extremePriceItemPerCategory.put(
                        lowest.getCategoryName(),
                        ExtremePriceItemByCategory.builder()
                                .categoryName(lowest.getCategoryName())
                                .lowestItem(Collections.singletonList(new BrandAndPriceDto(lowest.getBrandName(), lowest.getPrice())))
                                .highestItem(Collections.singletonList(new BrandAndPriceDto(highest.getBrandName(), highest.getPrice())))
                                .build()
                );
            }
        }
    }
}

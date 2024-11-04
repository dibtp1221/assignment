package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.BrandAndPriceDto;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.response.ExtremePriceItemByCategory;
import com.musinsa.assignment.domain.dto.response.LowestPriceItemPerCategory;
import com.musinsa.assignment.domain.dto.response.LowestTotalBrand;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.exhandle.exception.NeedMonitorException;
import com.musinsa.assignment.repository.fake.ItemSortRepositoryFakeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseCachingServiceTest {

    private final ItemSortRepositoryFakeImpl itemSortRepositoryFakeImpl = new ItemSortRepositoryFakeImpl();

    private final ResponseCachingService responseCachingService
            = new ResponseCachingService(new ItemSortService(itemSortRepositoryFakeImpl));

    @BeforeEach
    void setUp() {
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000
        itemSortRepositoryFakeImpl.setDefaultItems();
        responseCachingService.updateCache();
    }

    @Test
    @DisplayName("캐시된 카테고리별 최저가 상품 검증")
    void getLowestPriceItemPerCategory() {
        LowestPriceItemPerCategory result = responseCachingService.getLowestPriceItemPerCategory();
        assertThat(result.getTotalPrice().intValue()).isEqualTo(6000);

        List<ItemDto> itemDtos = result.getItems();
        assertThat(itemDtos).containsExactly(
                new ItemDto("top", "brand1", 2000),
                new ItemDto("outer", "brand2", 4000)
        );
    }

    @Test
    @DisplayName("캐시된 카테고리별 최저가, 최고가 상품 검증")
    void getExtremePriceItemsByCategory() {
        ExtremePriceItemByCategory top = responseCachingService.getExtremePriceItemsByCategory("top");
        assertThat(top.getCategoryName()).isEqualTo("top");
        assertThat(top.getLowestItem()).containsExactly(new BrandAndPriceDto("brand1", 2000));
        assertThat(top.getHighestItem()).containsExactly(new BrandAndPriceDto("brand2", 3500));

        ExtremePriceItemByCategory outer = responseCachingService.getExtremePriceItemsByCategory("outer");
        assertThat(outer.getCategoryName()).isEqualTo("outer");
        assertThat(outer.getLowestItem()).containsExactly(new BrandAndPriceDto("brand2", 4000));
        assertThat(outer.getHighestItem()).containsExactly(new BrandAndPriceDto("brand1", 5000));
    }

    @Test
    @DisplayName("캐시된 컬렉션은 unmodifiable이므로 변경시도하면 예외발생")
    void getResponse() {
        LowestPriceItemPerCategory result = responseCachingService.getLowestPriceItemPerCategory();
        List<ItemDto> itemDtos = result.getItems();
        assertThatThrownBy(
                () -> itemDtos.add(
                        new ItemDto(
                                new Item(1000, new Brand("addTest"), new Category("ctg", 1))
                        )
                )
        ).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("조회되는 데이터 변경 & 캐시 업데이트 -> 원래 있었던 카테고리 없어진 것, 데이터 변경된 것 확인")
    void dataChange() {
        itemSortRepositoryFakeImpl.setItems(
            List.of(
                new Item(2000, new Brand("lowestBrand"), new Category("outer", 2)),
                new Item(3000, new Brand("lowestBrand"), new Category("pants", 3)),

                new Item(6000, new Brand("highestBrand"), new Category("outer", 2)),
                new Item(7000, new Brand("highestBrand"), new Category("pants", 3))
            )
        );

        // 캐시 업데이트
        responseCachingService.updateCache();

        // 상의 요청이 오면 예외 발생
        assertThatThrownBy(
                () -> responseCachingService.getExtremePriceItemsByCategory("top")
        ).isExactlyInstanceOf(NeedMonitorException.class);

        // 각 카테고리별 최고가, 최저가 데이터 확인
        ExtremePriceItemByCategory outer = responseCachingService.getExtremePriceItemsByCategory("outer");
        assertThat(outer.getCategoryName()).isEqualTo("outer");
        assertThat(outer.getLowestItem()).containsExactly(new BrandAndPriceDto("lowestBrand", 2000));
        assertThat(outer.getHighestItem()).containsExactly(new BrandAndPriceDto("highestBrand", 6000));

        ExtremePriceItemByCategory pants = responseCachingService.getExtremePriceItemsByCategory("pants");
        assertThat(pants.getCategoryName()).isEqualTo("pants");
        assertThat(pants.getLowestItem()).containsExactly(new BrandAndPriceDto("lowestBrand", 3000));
        assertThat(pants.getHighestItem()).containsExactly(new BrandAndPriceDto("highestBrand", 7000));

    }

    @Test
    @DisplayName("캐시된 각 브랜드별 총액 최저가 상품 검증")
    void getLowestTotalBrand() {
        LowestTotalBrand result = responseCachingService.getLowestTotalBrand();
        assertThat(result.getBrandName()).isEqualTo("brand1");
        assertThat(result.getTotalPrice().intValue()).isEqualTo(7000);
    }

    @Test
    @DisplayName("상품 하나의 가격이 최저가로 변경 -> 캐시 업데이트")
    void updateCacheSingleItemLowest() {
        // AS-IS
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000

        // TO-BE
        // brand1 - top: 2000 -> 1000

        itemSortRepositoryFakeImpl.changeItem("brand1", "top", 1000);

        responseCachingService.updateCacheSingleItem(
                new Item(1000, new Brand("brand1"), new Category("top", 1))
        );

        // 카테고리 별 최저가격 변경
        LowestPriceItemPerCategory lowestPriceItemPerCategory = responseCachingService.getLowestPriceItemPerCategory();
        Optional<ItemDto> top = lowestPriceItemPerCategory.getItems().stream().filter(it -> it.getCategoryName().equals("top")).findAny();
        assertThat(top).isPresent();
        assertThat(top.get().getPrice().intValue()).isEqualTo(1000);

        // 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격 조회했을 때 최저가 상품정보 변경
        ExtremePriceItemByCategory extremeItems = responseCachingService.getExtremePriceItemsByCategory("top");
        assertThat(extremeItems.getLowestItem().get(0)).isEqualTo(new BrandAndPriceDto("brand1", 1000));

        // 단일 브랜드 최저가격 변경
        LowestTotalBrand lowestTotalBrand = responseCachingService.getLowestTotalBrand();
        assertThat(lowestTotalBrand.getBrandName()).isEqualTo("brand1");
        assertThat(lowestTotalBrand.getTotalPrice().intValue()).isEqualTo(6000);
    }

    @Test
    @DisplayName("상품 하나의 가격이 변경되었을 때 캐시 업데이트 - 최고가")
    void updateCacheSingleItemHighest() {

        // AS-IS
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000

        // TO-BE
        // brand2 - top: 3500 -> 5000

        itemSortRepositoryFakeImpl.changeItem("brand2", "top", 5000);

        responseCachingService.updateCacheSingleItem(
                new Item(5000, new Brand("brand2"), new Category("top", 1))
        );

        // 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격 조회했을 때 최저가 상품정보 변경
        ExtremePriceItemByCategory extremeItems = responseCachingService.getExtremePriceItemsByCategory("top");
        assertThat(extremeItems.getHighestItem().get(0)).isEqualTo(new BrandAndPriceDto("brand2", 5000));

        // 단일 브랜드 최저가격 변경 없음
        LowestTotalBrand lowestTotalBrand = responseCachingService.getLowestTotalBrand();
        assertThat(lowestTotalBrand.getBrandName()).isEqualTo("brand1");
        assertThat(lowestTotalBrand.getTotalPrice().intValue()).isEqualTo(7000);
    }

    @Test
    @DisplayName("단일 브랜드로 했을 때 총액 합 최저가인 브랜드 변경")
    void lowestSumBrandChange() {

        // AS-IS
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000

        LowestTotalBrand lowestTotalBrand = responseCachingService.getLowestTotalBrand();
        assertThat(lowestTotalBrand.getBrandName()).isEqualTo("brand1");
        assertThat(lowestTotalBrand.getTotalPrice().intValue()).isEqualTo(7000);

        // TO-BE
        // brand1 - top: 2000 -> 3000

        itemSortRepositoryFakeImpl.changeItem("brand1", "top", 3000);

        responseCachingService.updateCacheSingleItem(
                new Item(3000, new Brand("brand1"), new Category("top", 1))
        );

        LowestTotalBrand newLowestTotalBrand = responseCachingService.getLowestTotalBrand();
        assertThat(newLowestTotalBrand.getBrandName()).isEqualTo("brand2");
        assertThat(newLowestTotalBrand.getTotalPrice().intValue()).isEqualTo(7500);
    }

    @Test
    @DisplayName("최저가로 캐싱되어있던 상품 가격이 바뀌면 최저가 상품 바뀌어야")
    void cachedAsLowestItemChanged() {

        // AS-IS
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000

        // to-be
        // brand1 - top: 2000 -> 2200

        itemSortRepositoryFakeImpl.changeItem("brand1", "top", 2200);
        responseCachingService.updateCacheSingleItem(
                new Item(2200, new Brand("brand1"), new Category("top", 1))
        );

        responseCachingService.getLowestPriceItemPerCategory().getItems()
                .forEach(it -> {
                    if (it.getCategoryName().equals("top")) {
                        assertThat(it.getPrice().intValue()).isEqualTo(2200);
                    }
                });

        responseCachingService.getExtremePriceItemsByCategory("top").getLowestItem()
                .forEach(it -> assertThat(it.getPrice().intValue()).isEqualTo(2200));
    }

    @Test
    @DisplayName("최고가로 캐싱되어있던 상품 가격이 바뀌면 최고가 상품 바뀌어야")
    void cachedAsHighestItemChanged() {

        // AS-IS
        // brand1 - top: 2000, outer: 5000
        // brand2 - top: 3500, outer: 4000

        // to-be
        // brand2 - top: 3500 -> 3700

        itemSortRepositoryFakeImpl.changeItem("brand2", "top", 3700);
        responseCachingService.updateCacheSingleItem(
                new Item(3700, new Brand("brand2"), new Category("top", 1))
        );

        responseCachingService.getExtremePriceItemsByCategory("top").getHighestItem()
                .forEach(it -> assertThat(it.getPrice().intValue()).isEqualTo(3700));
    }
}
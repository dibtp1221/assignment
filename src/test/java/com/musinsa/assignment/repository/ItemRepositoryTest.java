package com.musinsa.assignment.repository;

import com.musinsa.assignment.domain.dto.BrandTotalPriceDto;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static com.musinsa.assignment.initdata.DataInitConstUtil.*;
import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Junit5에서 static method를 사용하지 않고도 @BeforeAll을 사용할 수 있게 해줌
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeAll
    void setData() {
        brandJpaRepository.save(A);
        brandJpaRepository.save(B);
        brandJpaRepository.save(C);

        List<Category> categories = List.of(TOP, OUTER, PANTS, SNEAKERS, BAG, HAT, SOCKS, ACCESSORIES);
        categoryJpaRepository.saveAll(categories);

        HashMap<Brand, HashMap<Category, Integer>> items = new HashMap<>();

        items.put(A,
                makeItemMapOfBrand(11200, 5500, 3000, 9000,
                        2000, 1700, 1800, 2300));
        items.put(B,
                makeItemMapOfBrand(11200, 5900, 3000, 9100,
                        2100, 2000, 2000, 2200));
        items.put(C,
                makeItemMapOfBrand(11200, 6200,3000, 9200,
                        2200, 1900, 2200, 2100));

        items.forEach((brand, map) ->
            map.forEach((category, price) -> itemRepository.save(new Item(price, brand, category)))
        );
    }

    @Test
    @DisplayName("카테고리별 가장 낮은 가격 상품 조회")
    void findLowestPriceItemPerCategory() {
        List<Item> items = itemRepository.findLowestPriceItemPerCategory();
        assertThat(items).hasSize(8);

        HashMap<Category, Integer> expectedPrices = new HashMap<>();
        expectedPrices.put(TOP, 11200);
        expectedPrices.put(OUTER, 5500);
        expectedPrices.put(PANTS, 3000);
        expectedPrices.put(SNEAKERS, 9000);
        expectedPrices.put(BAG, 2000);
        expectedPrices.put(HAT, 1700);
        expectedPrices.put(SOCKS, 1800);
        expectedPrices.put(ACCESSORIES, 2100);


        HashMap<Category, Brand> expectedBrands = new HashMap<>();
        expectedBrands.put(TOP, C);
        expectedBrands.put(OUTER, A);
        expectedBrands.put(PANTS, C);
        expectedBrands.put(SNEAKERS, A);
        expectedBrands.put(BAG, A);
        expectedBrands.put(HAT, A);
        expectedBrands.put(SOCKS, A);
        expectedBrands.put(ACCESSORIES, C);

        items.forEach(item -> {
            assertThat(item.getPrice().intValue()).isEqualTo(expectedPrices.get(item.getCategory()));
            assertThat(item.getBrand()).isEqualTo(expectedBrands.get(item.getCategory()));
        });
    }

    @Test
    @DisplayName("카테고리별 가장 높은 가격 상품 조회")
    void findHighestPriceItemPerCategory() {
        List<Item> items = itemRepository.findHighestPriceItemPerCategory();
        assertThat(items).hasSize(8);

        HashMap<Category, Integer> expectedPrices = new HashMap<>();
        expectedPrices.put(TOP, 11200);
        expectedPrices.put(OUTER, 6200);
        expectedPrices.put(PANTS, 3000);
        expectedPrices.put(SNEAKERS, 9200);
        expectedPrices.put(BAG, 2200);
        expectedPrices.put(HAT, 2000);
        expectedPrices.put(SOCKS, 2200);
        expectedPrices.put(ACCESSORIES, 2300);

        HashMap<Category, Brand> expectedBrands = new HashMap<>();
        expectedBrands.put(TOP, C);
        expectedBrands.put(OUTER, C);
        expectedBrands.put(PANTS, C);
        expectedBrands.put(SNEAKERS, C);
        expectedBrands.put(BAG, C);
        expectedBrands.put(HAT, B);
        expectedBrands.put(SOCKS, C);
        expectedBrands.put(ACCESSORIES, A);

        items.forEach(item -> {
            assertThat(item.getPrice().intValue()).isEqualTo(expectedPrices.get(item.getCategory()));
            assertThat(item.getBrand()).isEqualTo(expectedBrands.get(item.getCategory()));
        });
    }


    @Test
    @DisplayName("특정 카테고리 가장 낮은 가격 상품 조회")
    void findLowestPriceItemByCategory() {
        Item lowestPants = itemRepository.findLowestPriceItemByCategory(PANTS).orElseThrow();
        assertThat(lowestPants.getPrice().intValue()).isEqualTo(3000);
        assertThat(lowestPants.getCategory()).isEqualTo(PANTS);
        assertThat(lowestPants.getBrand()).isEqualTo(C);

        Item lowestOuter = itemRepository.findLowestPriceItemByCategory(OUTER).orElseThrow();
        assertThat(lowestOuter.getPrice().intValue()).isEqualTo(5500);
        assertThat(lowestOuter.getCategory()).isEqualTo(OUTER);
        assertThat(lowestOuter.getBrand()).isEqualTo(A);
    }

    @Test
    @DisplayName("특정 카테고리 가장 높은 가격 상품 조회")
    void findHighestPriceItemByCategory() {
        Item item = itemRepository.findHighestPriceItemByCategory(PANTS).orElseThrow();
        assertThat(item.getPrice().intValue()).isEqualTo(3000);
        assertThat(item.getCategory()).isEqualTo(PANTS);
        assertThat(item.getBrand()).isEqualTo(C);

        Item lowestOuter = itemRepository.findHighestPriceItemByCategory(OUTER).orElseThrow();
        assertThat(lowestOuter.getPrice().intValue()).isEqualTo(6200);
        assertThat(lowestOuter.getCategory()).isEqualTo(OUTER);
        assertThat(lowestOuter.getBrand()).isEqualTo(C);
    }

    @Test
    @DisplayName("총 가격이 가장 낮은 브랜드 조회")
    void findBrandWithLowestTotalPrice() {
        List<BrandTotalPriceDto> lowestPriceBrand = itemRepository.findBrandWithLowestTotalPrice();
        assertThat(lowestPriceBrand).hasSize(3);

        BrandTotalPriceDto brandTotalPriceDto = lowestPriceBrand.get(0);
        assertThat(brandTotalPriceDto.getBrand()).isEqualTo(A);
        assertThat(brandTotalPriceDto.getTotalPrice().intValue()).isEqualTo(36500);
    }

    @Test
    @DisplayName("브랜드로 상품 조회")
    void findItemsByBrand_A() {
        List<Item> items = itemRepository.findItemsByBrand(A);
        assertThat(items).hasSize(8);

        List<Category> expectedCategories = List.of(TOP, OUTER, PANTS, SNEAKERS, BAG, HAT, SOCKS, ACCESSORIES);
        List<Integer> expectedPrices = List.of(11200, 5500, 3000, 9000, 2000, 1700, 1800, 2300);

        for (int i = 0; i < items.size(); i++) {
            assertThat(items.get(i).getCategory()).isEqualTo(expectedCategories.get(i));
            assertThat(items.get(i).getPrice().intValue()).isEqualTo(expectedPrices.get(i));
        }
    }
}
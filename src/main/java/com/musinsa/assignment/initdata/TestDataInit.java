package com.musinsa.assignment.initdata;

import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.repository.BrandJpaRepository;
import com.musinsa.assignment.repository.CategoryJpaRepository;
import com.musinsa.assignment.repository.ItemRepository;
import com.musinsa.assignment.service.ResponseCachingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.List;

import static com.musinsa.assignment.initdata.DataInitConstUtil.*;

public class TestDataInit {

    private final ItemRepository itemRepository;
    private final BrandJpaRepository brandRepository;
    private final CategoryJpaRepository categoryRepository;
    private final ResponseCachingService responseCachingService;

    private final List<Brand> brands = List.of(A, B, C, D, E, F, G, H, I);

    private final List<Category> categories = List.of(TOP, OUTER, PANTS, SNEAKERS, BAG, HAT, SOCKS, ACCESSORIES);

    public TestDataInit(ItemRepository itemRepository, BrandJpaRepository brandRepository, CategoryJpaRepository categoryRepository, ResponseCachingService responseCachingService) {
        this.itemRepository = itemRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.responseCachingService = responseCachingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        HashMap<Brand, HashMap<Category, Integer>> items = new HashMap<>();

        items.put(A,
                makeItemMapOfBrand(11200, 5500, 4200, 9000,
                2000, 1700, 1800, 2300));
        items.put(B,
                makeItemMapOfBrand(10500, 5900, 3800, 9100,
                2100, 2000, 2000, 2200));
        items.put(C,
                makeItemMapOfBrand(10000, 6200,3300, 9200,
                2200, 1900, 2200, 2100));
        items.put(D,
                makeItemMapOfBrand(10100, 5100, 3000, 9500,
                2500, 1500, 2400, 2000));
        items.put(E,
                makeItemMapOfBrand(10700, 5000, 3800, 9900,
                2300, 1800, 2100, 2100));
        items.put(F,
                makeItemMapOfBrand(11200, 7200, 4000, 9300,
                2100, 1600, 2300, 1900));
        items.put(G,
                makeItemMapOfBrand(10500, 5800, 3900, 9000,
                2200, 1700, 2100, 2000));
        items.put(H,
                makeItemMapOfBrand(10800, 6300, 3100, 9700,
                2100, 1600, 2000, 2000));
        items.put(I,
                makeItemMapOfBrand(11400, 6700, 3200, 9500,
                2400, 1700, 1700, 2400));

        brandRepository.saveAll(brands);
        categoryRepository.saveAll(categories);
        items.forEach(
                (brand, map) -> map.forEach((category, price) -> itemRepository.save(new Item(price, brand, category)))
        );
        responseCachingService.updateCache();
    }

}

package com.musinsa.assignment.repository.fake;

import com.musinsa.assignment.domain.dto.BrandTotalPriceDto;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.repository.ItemSortRepository;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Setter
public class ItemSortRepositoryFakeImpl implements ItemSortRepository {

    private List<Item> items;

    public ItemSortRepositoryFakeImpl() {
        setDefaultItems();
    }

    public void setDefaultItems() {
        items = List.of(
                new Item(2000, new Brand("brand1"), new Category("top", 1)),
                new Item(5000, new Brand("brand1"), new Category("outer", 2)),

                new Item(3500, new Brand("brand2"), new Category("top", 1)),
                new Item(4000, new Brand("brand2"), new Category("outer", 2))
        );
    }

    public void changeItem(String brand, String category, int price) {
        items.stream().filter(it -> it.getBrandName().equals(brand) && it.getCategoryName().equals(category))
                .findFirst().ifPresent(it -> it.setPrice(price));
    }

    @Override
    public List<Item> findLowestPriceItemPerCategory() {
        return items.stream()
                .collect(Collectors.groupingBy(Item::getCategory, Collectors.minBy(Comparator.comparing(Item::getPrice))))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(item -> item.getCategory().getSortOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findHighestPriceItemPerCategory() {
        return items.stream()
                .collect(Collectors.groupingBy(Item::getCategory, Collectors.maxBy(Comparator.comparing(Item::getPrice))))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(item -> item.getCategory().getSortOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findLowestPriceItemByCategory(Category category) {
        return findLowestPriceItemPerCategory().stream().filter(item -> item.getCategory().equals(category)).findFirst();
    }

    @Override
    public Optional<Item> findHighestPriceItemByCategory(Category category) {
        return findHighestPriceItemPerCategory().stream().filter(item -> item.getCategory().equals(category)).findFirst();
    }

    @Override
    public List<BrandTotalPriceDto> findBrandWithLowestTotalPrice() {
        return items.stream()
                .collect(Collectors.groupingBy(Item::getBrand, Collectors.reducing(BigDecimal.ZERO, Item::getPrice, BigDecimal::add)))
                .entrySet().stream()
                .map(entry -> new BrandTotalPriceDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(BrandTotalPriceDto::getTotalPrice))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemsByBrand(Brand brand) {
        return items.stream().filter(item -> item.getBrand().equals(brand)).collect(Collectors.toList());
    }

}

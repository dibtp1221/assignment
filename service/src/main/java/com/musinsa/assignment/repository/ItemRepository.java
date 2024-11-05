package com.musinsa.assignment.repository;

import com.musinsa.assignment.domain.dto.BrandTotalPriceDto;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends ItemSortRepository, JpaRepository<Item, Long> {

    Optional<Item> findByCategoryAndBrand(Category category, Brand brand);

    @Override
    @Query("SELECT i FROM Item i join fetch i.category join fetch i.brand " +
            "WHERE i.price = (SELECT MIN(i2.price) FROM Item i2 WHERE i2.category = i.category) " +
            "AND i.createdDate = (SELECT MAX(i3.createdDate) FROM Item i3 WHERE i3.category = i.category and i3.price = i.price) " +
            "order by i.category.sortOrder")
    List<Item> findLowestPriceItemPerCategory();

    @Override
    @Query("SELECT i FROM Item i join fetch i.category join fetch i.brand " +
            "WHERE i.price = (SELECT MAX(i2.price) FROM Item i2 WHERE i2.category = i.category) " +
            "AND i.createdDate = (SELECT MAX(i3.createdDate) FROM Item i3 WHERE i3.category = i.category and i3.price = i.price)" +
            "order by i.category.sortOrder")
    List<Item> findHighestPriceItemPerCategory();

    @Override
    @Query("SELECT i FROM Item i join fetch i.category join fetch i.brand " +
            "WHERE i.category = :category " +
            "AND i.price = (SELECT MIN(i2.price) FROM Item i2 WHERE i2.category = :category) " +
            "AND i.createdDate = (SELECT MAX(i3.createdDate) FROM Item i3 WHERE i3.category = :category AND i3.price = i.price)")
    Optional<Item> findLowestPriceItemByCategory(@Param("category") Category category);

    @Override
    @Query("SELECT i FROM Item i join fetch i.category join fetch i.brand " +
       "WHERE i.category = :category " +
       "AND i.price = (SELECT MAX(i2.price) FROM Item i2 WHERE i2.category = :category) " +
       "AND i.createdDate = (SELECT MAX(i3.createdDate) FROM Item i3 WHERE i3.category = :category AND i3.price = i.price)")
    Optional<Item> findHighestPriceItemByCategory(@Param("category") Category category);

    @Override
    @Query("SELECT new com.musinsa.assignment.domain.dto.BrandTotalPriceDto(i.brand, SUM(i.price)) " +
       "FROM Item i " +
       "GROUP BY i.brand " +
       "ORDER BY SUM(i.price) ASC")
    List<BrandTotalPriceDto> findBrandWithLowestTotalPrice();

    @Override
    @Query("SELECT i FROM Item i JOIN FETCH i.category JOIN FETCH i.brand " +
            "WHERE i.brand = :brand " +
            "order by i.category.sortOrder")
    List<Item> findItemsByBrand(@Param("brand") Brand brand);
}

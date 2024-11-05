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

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByCategoryAndBrand(Category category, Brand brand);

}

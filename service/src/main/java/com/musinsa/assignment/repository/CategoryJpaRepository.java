package com.musinsa.assignment.repository;

import com.musinsa.assignment.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String name);
}

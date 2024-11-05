package com.musinsa.assignment.service;

import com.musinsa.assignment.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryJpaRepository categoryJpaRepository;

    public boolean isExistCategory(String categoryName) {
        return categoryJpaRepository.findByCategoryName(categoryName).isPresent();
    }
}

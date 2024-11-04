package com.musinsa.assignment.repository;

import com.musinsa.assignment.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByBrandName(String name);
}

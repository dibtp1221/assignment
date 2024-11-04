package com.musinsa.assignment.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Entity
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 카테고리는 int의 범위로도 충분하다고 생각해서 int로 처리.

    @Getter
    @Column(name = "category_name")
    private String categoryName;

    @Getter
    @Column(name = "sort_order")
    private Integer sortOrder;

    public Category() {
    }

    public Category(String categoryName, int sortOrder) {
        this.categoryName = categoryName;
        this.sortOrder = sortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(categoryName, category.categoryName) && Objects.equals(sortOrder, category.sortOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryName, sortOrder);
    }
}

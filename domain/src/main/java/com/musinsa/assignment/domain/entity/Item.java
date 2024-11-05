package com.musinsa.assignment.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Item() {
    }

    public Item(int price, Brand brand, Category category) {
        this.price = BigDecimal.valueOf(price);
        this.brand = brand;
        this.category = category;
    }
    public Item(BigDecimal price, Brand brand, Category category) {
        this.price = price;
        this.brand = brand;
        this.category = category;
    }

    @Override
    public String toString() {
        return "Item{" +
                "category=" + category.getCategoryName() +
                ", brand=" + brand.getBrandName() +
                ", price=" + price +
                '}';
    }

    public String getCategoryName() {
        return category.getCategoryName();
    }

    public String getBrandName() {
        return brand.getBrandName();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPrice(int price) {
        this.price = BigDecimal.valueOf(price);
    }
}

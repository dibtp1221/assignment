package com.musinsa.assignment.initdata;

import com.musinsa.assignment.repository.BrandJpaRepository;
import com.musinsa.assignment.repository.CategoryJpaRepository;
import com.musinsa.assignment.repository.ItemRepository;
import com.musinsa.assignment.service.CacheMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class TestDataInitTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BrandJpaRepository brandRepository;
    @Autowired
    private CategoryJpaRepository categoryRepository;
    @Autowired
    private CacheMessageProducer cacheMessageProducer;

    @Test
    void initializeData() {
        TestDataInit testDataInit
                = new TestDataInit(itemRepository, brandRepository, categoryRepository, cacheMessageProducer);
        testDataInit.initializeData();
        assertThat(itemRepository.findAll()).hasSize(72);
        assertThat(brandRepository.findAll()).hasSize(9);
        assertThat(categoryRepository.findAll()).hasSize(8);
    }
}
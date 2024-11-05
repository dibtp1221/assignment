package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.ItemMessageDto;
import com.musinsa.assignment.domain.dto.inner.ResultDto;
import com.musinsa.assignment.domain.entity.Brand;
import com.musinsa.assignment.domain.entity.Category;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.exhandle.exception.UserException;
import com.musinsa.assignment.repository.BrandJpaRepository;
import com.musinsa.assignment.repository.CategoryJpaRepository;
import com.musinsa.assignment.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 아이템 가격 업데이트 서비스
 */
@Service
@RequiredArgsConstructor
public class ItemUpdateService {

    private final ItemRepository itemRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    private final CacheMessageProducer cacheMessageProducer;

    // 브랜드 하나 - 아이템들 전체 다 같이 추가

    /**
     * 아이템 가격 업데이트 - 카테고리, 브랜드명으로
     */
    public ResultDto<ItemDto> updateItemPrice(ItemDto itemDto) {
        Category category = categoryJpaRepository.findByCategoryName(itemDto.getCategoryName()).orElseThrow();
        Brand brand = brandJpaRepository.findByBrandName(itemDto.getBrandName()).orElseThrow();

        Item item = itemRepository.findByCategoryAndBrand(category, brand).orElseThrow();
        return saveAndCache(item, itemDto.getPrice());
    }

    /**
     * 아이템 가격 업데이트 - 아이템 아이디로
     */
    public ResultDto<ItemDto> updateItemPrice(String itemId, BigDecimal price) {
        long id = changeItemIdType(itemId);

        Item item = itemRepository.findById(id).orElseThrow();
        return saveAndCache(item, price);
    }

    /**
     * itemId를 long으로 변환, 숫자가 아니면 예외 발생
     */
    private long changeItemIdType(String itemId) {
        try {
            return Long.parseLong(itemId);
        } catch (NumberFormatException e) {
            throw new UserException("itemId는 숫자여야 합니다.", "입력: " + itemId);
        }
    }

    /**
     * 아이템 저장 및 캐시 업데이트
     */
    private ResultDto<ItemDto> saveAndCache(Item item, BigDecimal price) {

        if (Objects.equals(item.getPrice(), price)) {
            return new ResultDto<>(false, "가격 변화 없어 변경 안함", new ItemDto(item));
        }

        // 가격 변경 -> db 저장
        item.setPrice(price);
        itemRepository.save(item);

        // 상품 단건 변경 전용 캐시 업데이트
        cacheMessageProducer.sendSingleItemCacheUpdate(ItemMessageDto.from(item));

        return new ResultDto<>(true, "변경 완료", new ItemDto(item));
    }
}

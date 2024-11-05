package com.musinsa.assignment.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.ItemMessageDto;
import com.musinsa.assignment.domain.entity.Item;
import com.musinsa.assignment.service.ResponseCachingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CacheUpdateConsumer {

    private final ResponseCachingService cachingService;

    public CacheUpdateConsumer(ResponseCachingService cachingService) {
        this.cachingService = cachingService;
    }

    /**
     * 전체 캐시 업데이트 메시지를 수신하는 메서드
     * @param message
     */
    @KafkaListener(topics = "cache.update.all", groupId = "group_1")
    public void updateAll(String message) {
        cachingService.updateAllCache(Long.valueOf(message));
    }
    /**
     * 상품 하나 업데이트
     */
    @KafkaListener(topics = "cache.update.single.item", groupId = "group_1")
    public void updateSingleItem(ItemMessageDto itemMessageDto) {
        cachingService.updateSingleItem(itemMessageDto);
    }
}

package com.musinsa.assignment.service;

import com.musinsa.assignment.domain.dto.ItemDto;
import com.musinsa.assignment.domain.dto.ItemMessageDto;
import com.musinsa.assignment.domain.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheMessageProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendAllCacheUpdate() {
        kafkaTemplate.send("cache.update.all", increment());
    }

    public void sendSingleItemCacheUpdate(ItemMessageDto item) {
        kafkaTemplate.send("cache.update.single.item", item);
    }

    private Long increment() {
        return redisTemplate.opsForValue().increment("producer:update:version");
    }
}

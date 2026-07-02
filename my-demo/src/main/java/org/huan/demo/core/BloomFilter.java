package org.huan.demo.core;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BloomFilter {

    public static final String BLOOM_FILTER_KEY = "studentFilter";
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @PostConstruct
    public void init() {
        String key = "student:8";
        long position = position(key);
        redisTemplate.opsForValue().setBit(BLOOM_FILTER_KEY, position, true);
        log.info("=== bloom filter of key:{},position:{}", key, position);
    }

    public boolean checkKey(String key) {
        long position = position(key);
        Boolean exist = redisTemplate.opsForValue().getBit(BLOOM_FILTER_KEY, position);
        log.info("=== check key:{},position:{},result:{}", key, position, exist);
        return Boolean.TRUE.equals(exist);
    }

    public long position(String key) {
        int hash = Math.abs(key.hashCode());
        return (long) (hash % Math.pow(2, 32));
    }

}

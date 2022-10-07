package com.simplefanc.voj.backend.config;

import com.simplefanc.voj.backend.cache.DoubleCacheManager;
import com.simplefanc.voj.backend.config.property.DoubleCacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author: chenfan
 * @create: 2022-10-6 10:07
 **/
@Configuration
public class CacheConfig {
    @Autowired
    DoubleCacheProperties doubleCacheConfig;

    @Bean
    public DoubleCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate,
                                           DoubleCacheProperties doubleCacheConfig) {
        return new DoubleCacheManager(redisTemplate, doubleCacheConfig);
    }
}

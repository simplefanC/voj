package com.simplefanc.voj.backend.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.simplefanc.voj.backend.config.property.DoubleCacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @program: 缓存管理器 管理 DoubleCache 作为spring中的缓存使用
 * @author: chenfan
 * @create: 2022-10-6 10:07
 **/
public class DoubleCacheManager implements CacheManager {
    Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private RedisTemplate<Object, Object> redisTemplate;
    private DoubleCacheProperties cacheConfig;

    public DoubleCacheManager(RedisTemplate<Object, Object> redisTemplate,
                              DoubleCacheProperties doubleCacheConfig) {
        this.redisTemplate = redisTemplate;
        this.cacheConfig = doubleCacheConfig;
    }

    /**
     * 根据cacheName获取Cache实例，不存在时进行创建
     * @param name
     * @return
     */
    @Override
    public Cache getCache(String name) {
        Cache cache = cacheMap.get(name);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        cache = new DoubleCache(name, redisTemplate, createCaffeineCache(name), cacheConfig);
        // 使用 ConcurrentHashMap的putIfAbsent()方法放入，避免重复创建Cache以及造成Cache内数据的丢失
        Cache oldCache = cacheMap.putIfAbsent(name, cache);
        return oldCache == null ? cache : oldCache;
    }

    /**
     * 返回管理的所有cacheName
     * @return
     */
    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    /**
     * 根据项目配置文件中的具体参数进行初始化
     * @return
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> createCaffeineCache(String name) {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();
        Optional<DoubleCacheProperties> cacheConfig = Optional.ofNullable(this.cacheConfig);
        cacheConfig.map(DoubleCacheProperties::getInitialCapacity)
                .ifPresent(caffeineBuilder::initialCapacity);
        cacheConfig.map(DoubleCacheProperties::getMaximumSize)
                .ifPresent(caffeineBuilder::maximumSize);
        final CacheTypeManager.CacheType cacheType = CacheTypeManager.CACHE_TYPE_MAP.get(name);
        if(cacheType != null) {
            caffeineBuilder.expireAfterWrite(cacheType.ttl1, TimeUnit.SECONDS);
        } else {
            cacheConfig.map(DoubleCacheProperties::getExpireAfterWrite)
                    .ifPresent(eaw -> caffeineBuilder.expireAfterWrite(eaw, TimeUnit.SECONDS));
        }
        cacheConfig.map(DoubleCacheProperties::getExpireAfterAccess)
                .ifPresent(eaa -> caffeineBuilder.expireAfterAccess(eaa, TimeUnit.SECONDS));
        cacheConfig.map(DoubleCacheProperties::getRefreshAfterWrite)
                .ifPresent(raw -> caffeineBuilder.refreshAfterWrite(raw, TimeUnit.SECONDS));
        return caffeineBuilder.build();
    }
}

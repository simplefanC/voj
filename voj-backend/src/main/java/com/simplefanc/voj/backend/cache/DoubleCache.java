package com.simplefanc.voj.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.simplefanc.voj.backend.config.property.DoubleCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: double-cache
 * @author: chenfan
 * @create: 2022-10-6 10:07
 **/
@Slf4j
public class DoubleCache extends AbstractValueAdaptingCache {
    private String cacheName;
    private RedisTemplate<Object, Object> redisTemplate;
    private Cache<Object, Object> caffeineCache;
    private DoubleCacheProperties cacheConfig;

    protected DoubleCache(boolean allowNullValues) {
        super(allowNullValues);
    }

    public DoubleCache(String cacheName, RedisTemplate<Object, Object> redisTemplate,
                       Cache<Object, Object> caffeineCache,
                       DoubleCacheProperties cacheConfig) {
        super(cacheConfig.getAllowNull());
        this.cacheName = cacheName;
        this.redisTemplate = redisTemplate;
        this.caffeineCache = caffeineCache;
        this.cacheConfig = cacheConfig;
    }

    /**
     * 在缓存中实际执行查找的操作，父类的get()方法会调用这个方法
     * @param key
     * @return
     */
    @Override
    protected Object lookup(Object key) {
        // 先从caffeine中查找
        Object obj = caffeineCache.getIfPresent(key);
        if (Objects.nonNull(obj)) {
            log.info("get data from caffeine");
            // 不用fromStoreValue，否则返回的是null，会再查数据库
            return obj;
        }

        // 再从redis中查找
        String redisKey = this.cacheName + ":" + key;
        obj = redisTemplate.opsForValue().get(redisKey);
        if (Objects.nonNull(obj)) {
            log.info("get data from redis");
            caffeineCache.put(key, obj);
        }
        return obj;
    }

    /**
     * 如果只是使用注解来管理缓存的话，那么这个方法不会被调用到（实际走父类的get方法）
     * @param key
     * @param valueLoader
     * @param <T>
     * @return
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();

            Object obj = lookup(key);
            if (Objects.nonNull(obj)) {
                return (T) obj;
            }
            // 没有找到
            obj = valueLoader.call();
            // 放入缓存
            put(key, obj);
            return (T) obj;
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * 将数据放入缓存中
     * @param key
     * @param value
     */
    @Override
    public void put(Object key, Object value) {
        if (!isAllowNullValues() && Objects.isNull(value)) {
            log.error("the value NULL will not be cached");
            return;
        }

        // 使用 toStoreValue(value) 包装，解决caffeine不能存null的问题
        caffeineCache.put(key, toStoreValue(value));

        // null对象只存在caffeine中一份就够了，不用存redis了
        if (Objects.isNull(value)) {
            return;
        }
        String redisKey = this.cacheName + ":" + key;
        final CacheTypeManager.CacheType cacheType = CacheTypeManager.CACHE_TYPE_MAP.get(this.cacheName);
        if(cacheType != null) {
            redisTemplate.opsForValue().set(redisKey, toStoreValue(value),
                    cacheType.ttl2, TimeUnit.SECONDS);
        } else {
            Optional<Long> expireOpt = Optional.ofNullable(cacheConfig)
                    .map(DoubleCacheProperties::getRedisExpire);
            if (expireOpt.isPresent()) {
                redisTemplate.opsForValue().set(redisKey, toStoreValue(value),
                        expireOpt.get(), TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(redisKey, toStoreValue(value));
            }
        }
    }

    /**
     * 删除缓存
     * @param key
     */
    @Override
    public void evict(Object key) {
        redisTemplate.delete(this.cacheName + ":" + key);
        caffeineCache.invalidate(key);
    }

    /**
     * 清空缓存中所有数据
     */
    @Override
    public void clear() {
        // 如果是正式环境，避免使用keys命令
        Set<Object> keys = redisTemplate.keys(this.cacheName.concat(":*"));
        for (Object key : keys) {
            redisTemplate.delete(String.valueOf(key));
        }
        caffeineCache.invalidateAll();
    }

    /**
     * 获取缓存名称，一般在CacheManager创建时指定
     * @return
     */
    @Override
    public String getName() {
        return this.cacheName;
    }

    /**
     * 获取实际使用的缓存
     * @return
     */
    @Override
    public Object getNativeCache() {
        return this;
    }

}

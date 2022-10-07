package com.simplefanc.voj.backend.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: double-cache
 * @author: chenfan
 * @create: 2022-10-6 10:07
 **/
@Data
@Component
@ConfigurationProperties(prefix = "voj.cache")
public class DoubleCacheProperties {
    private Boolean allowNull = true;
    /**
     * 缓存初始容量（能存储多少个缓存对象）
     */
    private Integer initialCapacity = 100;
    /**
     * 缓存最大容量
     */
    private Integer maximumSize = 1000;
    /**
     * 指定缓存的过期时间（写了之后多久过期）
     */
    private Long expireAfterWrite;
    private Long expireAfterAccess;
    private Long refreshAfterWrite;
    private Long redisExpire;
}

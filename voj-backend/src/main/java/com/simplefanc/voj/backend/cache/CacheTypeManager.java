package com.simplefanc.voj.backend.cache;

import com.simplefanc.voj.common.constants.RedisConstant;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenfan
 * @date 2022/10/6 16:06
 **/
public class CacheTypeManager {
    public static final Map<String, CacheType> CACHE_TYPE_MAP = new HashMap<>(){
        {
            put(RedisConstant.OI_CONTEST_RANK_CACHE, new CacheType(RedisConstant.OI_CONTEST_RANK_CACHE, 2 * 3600, 2 * 3600));
            put(RedisConstant.CONTEST_RANK_CAL_RESULT_CACHE, new CacheType(RedisConstant.CONTEST_RANK_CAL_RESULT_CACHE, 15, 15));
            put(RedisConstant.SUPER_ADMIN_UID_LIST_CACHE, new CacheType(RedisConstant.SUPER_ADMIN_UID_LIST_CACHE, 12 * 3600, 12 * 3600));
            put(RedisConstant.ACM_RANK_CACHE, new CacheType(RedisConstant.ACM_RANK_CACHE, 60, 60));
            put(RedisConstant.OI_RANK_CACHE, new CacheType(RedisConstant.OI_RANK_CACHE, 60, 60));
        }
    };

    @AllArgsConstructor
    public static class CacheType {
        public String key;
        public final int ttl1;
        public final int ttl2;
    }
}

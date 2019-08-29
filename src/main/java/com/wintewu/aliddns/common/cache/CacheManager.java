package com.wintewu.aliddns.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WinteWu
 */
public class CacheManager {

    private Map<String,Object> localCacheStore = new ConcurrentHashMap<>();

    private static CacheManager cacheManager = new CacheManager();


    private CacheManager() {

    }

    public static CacheManager getInstance() {
        return cacheManager;

    }


    public Object getValueByKey(String key) {
        return localCacheStore.get(key);

    }

    public String getStringValueByKey(String key) {
        return (String) localCacheStore.get(key);

    }

    public void putValue(String key, Object value) {
        localCacheStore.put(key, value);
    }

}

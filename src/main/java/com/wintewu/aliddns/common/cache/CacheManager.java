package com.wintewu.aliddns.common.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private Map localCacheStore = new HashMap();

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

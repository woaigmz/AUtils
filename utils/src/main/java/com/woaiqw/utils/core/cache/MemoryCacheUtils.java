package com.woaiqw.utils.core.cache;

import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haoran on 2019/2/18.
 */
public class MemoryCacheUtils implements CacheConstants {

    private static final int DEFAULT_MAX_COUNT = 256;

    private static final Map<String, MemoryCacheUtils> CACHE_MAP = new HashMap<>();

    private final String                       mCacheKey;
    private final LruCache<String, CacheValue> mMemoryCache;

    /**
     * Return the single {@link MemoryCacheUtils} instance.
     *
     * @return the single {@link MemoryCacheUtils} instance
     */
    public static MemoryCacheUtils getInstance() {
        return getInstance(DEFAULT_MAX_COUNT);
    }

    /**
     * Return the single {@link MemoryCacheUtils} instance.
     *
     * @param maxCount The max count of cache.
     * @return the single {@link MemoryCacheUtils} instance
     */
    public static MemoryCacheUtils getInstance(final int maxCount) {
        return getInstance(String.valueOf(maxCount), maxCount);
    }

    /**
     * Return the single {@link MemoryCacheUtils} instance.
     *
     * @param cacheKey The key of cache.
     * @param maxCount The max count of cache.
     * @return the single {@link MemoryCacheUtils} instance
     */
    public static MemoryCacheUtils getInstance(final String cacheKey, final int maxCount) {
        MemoryCacheUtils cache = CACHE_MAP.get(cacheKey);
        if (cache == null) {
            synchronized (MemoryCacheUtils.class) {
                cache = CACHE_MAP.get(cacheKey);
                if (cache == null) {
                    cache = new MemoryCacheUtils(cacheKey, new LruCache<String, CacheValue>(maxCount));
                    CACHE_MAP.put(cacheKey, cache);
                }
            }
        }
        return cache;
    }

    private MemoryCacheUtils(String cacheKey, LruCache<String, CacheValue> memoryCache) {
        mCacheKey = cacheKey;
        mMemoryCache = memoryCache;
    }

    @Override
    public String toString() {
        return mCacheKey + "@" + Integer.toHexString(hashCode());
    }

    /**
     * Put bytes in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    public void put(@NonNull final String key, final Object value) {
        put(key, value, -1);
    }

    /**
     * Put bytes in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    public void put(@NonNull final String key, final Object value, int saveTime) {
        if (value == null) return;
        long dueTime = saveTime < 0 ? -1 : System.currentTimeMillis() + saveTime * 1000;
        mMemoryCache.put(key, new CacheValue(dueTime, value));
    }

    /**
     * Return the value in cache.
     *
     * @param key The key of cache.
     * @param <T> The value type.
     * @return the value if cache exists or null otherwise
     */
    public <T> T get(@NonNull final String key) {
        return get(key, null);
    }

    /**
     * Return the value in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @param <T>          The value type.
     * @return the value if cache exists or defaultValue otherwise
     */
    public <T> T get(@NonNull final String key, final T defaultValue) {
        CacheValue val = mMemoryCache.get(key);
        if (val == null) return defaultValue;
        if (val.dueTime == -1 || val.dueTime >= System.currentTimeMillis()) {
            //noinspection unchecked
            return (T) val.value;
        }
        mMemoryCache.remove(key);
        return defaultValue;
    }

    /**
     * Return the count of cache.
     *
     * @return the count of cache
     */
    public int getCacheCount() {
        return mMemoryCache.size();
    }

    /**
     * Remove the cache by key.
     *
     * @param key The key of cache.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public Object remove(@NonNull final String key) {
        CacheValue remove = mMemoryCache.remove(key);
        if (remove == null) return null;
        return remove.value;
    }

    /**
     * Clear all of the cache.
     */
    public void clear() {
        mMemoryCache.evictAll();
    }

    private static final class CacheValue {
        long   dueTime;
        Object value;

        CacheValue(long dueTime, Object value) {
            this.dueTime = dueTime;
            this.value = value;
        }
    }
}

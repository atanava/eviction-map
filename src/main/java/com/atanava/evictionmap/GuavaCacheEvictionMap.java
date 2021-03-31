package com.atanava.evictionmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class GuavaCacheEvictionMap<K, V> implements EvictionMap<K, V> {
    private final Cache<K, V> cache;

    public GuavaCacheEvictionMap(long entryLifeTime) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(entryLifeTime, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }
}

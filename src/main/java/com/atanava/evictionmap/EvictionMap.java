package com.atanava.evictionmap;

public interface EvictionMap<K, V> {
    void put(K key, V value);
    V get(K key);
}

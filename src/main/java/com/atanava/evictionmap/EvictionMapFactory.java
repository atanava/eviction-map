package com.atanava.evictionmap;


public class EvictionMapFactory<K, V> {

    public EvictionMap<K, V> getEvictionMap(MapType type, long entryLifeTime) {

        EvictionMap<K, V> evictionMap;

        switch (type) {
            case THREAD_SAFE:
                evictionMap = new TheadSafeEvictionMap<>(entryLifeTime);
                break;
            case THREAD_UNSAFE:
                evictionMap = new TheadUnsafeEvictionMap<>(entryLifeTime);
                break;
            case GUAVA:
                evictionMap = new GuavaCacheEvictionMap<>(entryLifeTime);
                break;
            default:
                throw new IllegalArgumentException("Wrong Eviction Map type:" + type);
        }
        return evictionMap;
    }
}

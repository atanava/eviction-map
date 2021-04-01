package com.atanava.evictionmap;

import java.util.*;

public class TheadUnsafeEvictionMap<K, V> extends AbstractEvictionMap<K, V> {

    protected TheadUnsafeEvictionMap(long entryLifeTime) {
        super(new HashMap<>(), new LinkedList<>(), entryLifeTime);
    }

    @Override
    protected void evictIfNeeded() {
        evictCache();
    }
}

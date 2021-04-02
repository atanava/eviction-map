package com.atanava.evictionmap;


import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractEvictionMap<K, V> implements EvictionMap<K, V> {
    protected final Map<K, CompositeValue> innerMap;
    protected final Deque<CompositeKey> keysByAddingOrder;
    protected final long entryLifeTime;
    protected volatile int batchSize;
    protected final AtomicReference<Date> lastEvicted;
    private final Runtime runtime;


    protected AbstractEvictionMap(Map<K, CompositeValue> innerMap, Deque<CompositeKey> keysByAddingOrder,
                                  long entryLifeTime) {
        this.innerMap = innerMap;
        this.keysByAddingOrder = keysByAddingOrder;
        this.entryLifeTime = entryLifeTime;
        this.batchSize = 1000;
        this.lastEvicted = new AtomicReference<>(new Date());
        this.runtime = Runtime.getRuntime();
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void put(K key, V value) {
        Date now = new Date();
        CompositeValue compositeValue = new CompositeValue(now, value);
        CompositeKey compositeKey = new CompositeKey(now, key);

        innerMap.put(key, compositeValue);
        keysByAddingOrder.addLast(compositeKey);

        evictIfNeeded();
    }

    @Override
    public V get(K key) {
        evictIfNeeded();

        CompositeValue compositeValue = innerMap.get(key);
        if (compositeValue != null && (new Date().getTime() - compositeValue.inserted.getTime()) < entryLifeTime) {
            return compositeValue.value;
        }
        return null;
    }

    protected abstract void evictIfNeeded();

    protected void evictCache() {
        Date now = new Date();
        if ((now.getTime() - lastEvicted.get().getTime()) >= entryLifeTime) {
            Iterator<CompositeKey> iterator = keysByAddingOrder.iterator();
            while (iterator.hasNext()) {
                CompositeKey next = iterator.next();
                if ((now.getTime() - next.inserted.getTime()) >= entryLifeTime) {
                    iterator.remove();

                    K key = next.key;
                    if ((now.getTime() - innerMap.get(key).inserted.getTime()) >= entryLifeTime) {
                        innerMap.remove(key);
                    }
                } else break;
            }

            lastEvicted.set(now);
        }
    }

    protected class CompositeValue {
        final Date inserted;
        final V value;

        CompositeValue(Date inserted, V value) {
            this.inserted = inserted;
            this.value = value;
        }
    }

    protected class CompositeKey {
        final Date inserted;
        final K key;

        CompositeKey(Date inserted, K key) {
            this.inserted = inserted;
            this.key = key;
        }
    }

}

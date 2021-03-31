package com.atanava.evictionmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class EvictionMap<K, V> {

    private final Map<K, CompositeValue> innerMap;
    private final Deque<CompositeKey> keysByAddingOrder;
    private final long entryLifeTime;
    private final boolean isMultiThreaded;
    private final int batchSize;
    private final AtomicReference<Date> lastEvicted;

    public EvictionMap(long entryLifeTime, boolean isMultiThreaded, int batchSize) {
        this.entryLifeTime = entryLifeTime;
        this.isMultiThreaded = isMultiThreaded;
        this.batchSize = batchSize;
        this.lastEvicted = new AtomicReference<>(new Date());

        if (isMultiThreaded) {
            this.innerMap = new ConcurrentHashMap<>();
            this.keysByAddingOrder = new ConcurrentLinkedDeque<>();
            initCleaner();
        } else {
            this.innerMap = new HashMap<>();
            this.keysByAddingOrder = new LinkedList<>();
        }
    }

    private void initCleaner() {
        Thread cleaner = new Thread(new Cleaner());
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public void put(K key, V value) {
        Date now = new Date();
        CompositeValue compositeValue = new CompositeValue(now, value);
        CompositeKey compositeKey = new CompositeKey(now, key);

        innerMap.put(key, compositeValue);
        keysByAddingOrder.addLast(compositeKey);

        if (!isMultiThreaded && keysByAddingOrder.size() >= batchSize) {
            evictCache();
        }
    }

    public V get(K key) {
        if (!isMultiThreaded && keysByAddingOrder.size() >= batchSize) {
            evictCache();
        }

        CompositeValue compositeValue = innerMap.get(key);
        if (compositeValue != null && (new Date().getTime() - compositeValue.inserted.getTime()) < entryLifeTime) {
            return compositeValue.value;
        }
        return null;
    }

    private void evictCache() {

        if ((new Date().getTime() - lastEvicted.get().getTime()) >= entryLifeTime) {
            Iterator<CompositeKey> iterator = keysByAddingOrder.iterator();
            while (iterator.hasNext()) {
                CompositeKey next = iterator.next();
                Date now = new Date();
                if ((now.getTime() - next.inserted.getTime()) >= entryLifeTime) {
                    iterator.remove();

                    K key = next.key;
                    if ((now.getTime() - innerMap.get(key).inserted.getTime()) >= entryLifeTime) {
                        innerMap.remove(key);
                    }
                } else break;
            }

            lastEvicted.set(new Date());
        }
    }

    private class CompositeValue {
        private final Date inserted;
        private final V value;

        CompositeValue(Date inserted, V value) {
            this.inserted = inserted;
            this.value = value;
        }
    }

    private class CompositeKey {
        private final Date inserted;
        private final K key;

        private CompositeKey(Date inserted, K key) {
            this.inserted = inserted;
            this.key = key;
        }
    }

    private class Cleaner implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (keysByAddingOrder.size() >= batchSize) {
                    evictCache();
                }
            }
        }
    }

}
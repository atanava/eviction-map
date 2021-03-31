package com.atanava.evictionmap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class EvictionMap<K, V> {

    private final Map<K, CompositeValue> innerMap;
    private final Deque<CompositeKey> keysByAddingOrder;
    private final long entryLifeTime;
    private final ChronoUnit chronoUnit;
    private final boolean isMultiThreaded;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int evictionBatchSize;

    public EvictionMap(long entryLifeTime, ChronoUnit chronoUnit, boolean isMultiThreaded, int evictionBatchSize) {
        this.entryLifeTime = entryLifeTime;
        this.chronoUnit = chronoUnit;
        this.isMultiThreaded = isMultiThreaded;
        this.evictionBatchSize = evictionBatchSize;
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
        LocalDateTime now = LocalDateTime.now();
        CompositeValue compositeValue = new CompositeValue(now, value);
        CompositeKey compositeKey = new CompositeKey(now, key);
        innerMap.put(key, compositeValue);
        keysByAddingOrder.addLast(compositeKey);

        int count = counter.incrementAndGet();

        if (!isMultiThreaded && count >= evictionBatchSize) {
            evictCache();
        }
    }

    public V get(K key) {
        return innerMap.get(key).inserted.plus(entryLifeTime, chronoUnit).isAfter(LocalDateTime.now())
                ? innerMap.get(key).value
                : null;
    }

    private void evictCache() {
        Iterator<CompositeKey> iterator = keysByAddingOrder.iterator();
        while (iterator.hasNext()) {
            CompositeKey next = iterator.next();
            if (next.inserted.plus(entryLifeTime, chronoUnit).isBefore(LocalDateTime.now())) {
                iterator.remove();

                K key = next.key;
                if (innerMap.get(key).inserted.plus(entryLifeTime, chronoUnit).isBefore(LocalDateTime.now())) {
                    innerMap.remove(key);
                    counter.decrementAndGet();
                }
            } else break;
        }

    }

    private class CompositeValue {
        private final LocalDateTime inserted;
        private final V value;

        CompositeValue(LocalDateTime inserted, V value) {
            this.inserted = inserted;
            this.value = value;
        }
    }

    private class CompositeKey {
        private final LocalDateTime inserted;
        private final K key;

        private CompositeKey(LocalDateTime inserted, K key) {
            this.inserted = inserted;
            this.key = key;
        }
    }

    private class Cleaner implements Runnable {
        @Override
        public void run() {
            while (! Thread.currentThread().isInterrupted()) {
                if (counter.get() >= evictionBatchSize) {
                    evictCache();
                }
            }
        }
    }

}
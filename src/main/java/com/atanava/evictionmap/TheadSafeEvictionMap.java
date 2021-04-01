package com.atanava.evictionmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TheadSafeEvictionMap<K, V> extends AbstractEvictionMap<K, V> {

    protected TheadSafeEvictionMap(long entryLifeTime) {
        super(new ConcurrentHashMap<>(), new ConcurrentLinkedDeque<>(), entryLifeTime);
        initCleaner();
    }

    private void initCleaner() {
        Thread cleaner = new Thread(new Cleaner());
        cleaner.setDaemon(true);
        cleaner.start();
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

    @Override
    protected void evictIfNeeded() {
    }

}

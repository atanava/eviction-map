package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
class EvictionMapTest {

    private EvictionMap<Integer, String> evictionMap;
    private long lifeTimeMillis;
    private String expected;

    @BeforeEach
    void init() {
        lifeTimeMillis = 10_000;
        evictionMap = new EvictionMap<>(lifeTimeMillis, true, 1000);
        expected = "Item ";
    }

    @Test
    void putAndGetOnce() throws InterruptedException {
        evictionMap.put(1, expected);
        assertEquals(expected, evictionMap.get(1));
        Thread.sleep(10_000);
        assertNull(evictionMap.get(1));
    }

    @Test
    void frequentPutAndGetMultiThreaded() {

    }

    @Test
    void frequentPutAndGet() throws InterruptedException {
        Map<Integer, Date> timeMap = new HashMap<>();
        int batchSize = 1_000_000;

        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i, expected + i);
            timeMap.put(i, new Date());
        }

        Thread.sleep(lifeTimeMillis);

        int found = 0;
        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i + batchSize, expected + i + batchSize);
            if (new Date().getTime() - timeMap.get(i).getTime() < lifeTimeMillis) {
                assertEquals(expected + i, evictionMap.get(i));
            } else {
                assertNull(evictionMap.get(i));
            }
            found++;
        }
        assertEquals(batchSize, found);
    }
}
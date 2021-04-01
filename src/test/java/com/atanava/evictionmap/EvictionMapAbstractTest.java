package com.atanava.evictionmap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
abstract class EvictionMapAbstractTest {

    protected EvictionMapFactory<Integer, String> factory = new EvictionMapFactory<>();
    protected EvictionMap<Integer, String> evictionMap;
    protected long lifeTimeMillis = 10_000;
    protected String expected = "Item ";

    @Test
    void putAndGetOnce() throws InterruptedException {
        evictionMap.put(1, expected);
        assertEquals(expected, evictionMap.get(1));
        Thread.sleep(10_000);
        assertNull(evictionMap.get(1));
    }


    @Test
    void frequentPutAndGet() throws InterruptedException {
        Map<Integer, Date> timeMap = new HashMap<>();
        int batchSize = 1_000_000;

        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i, expected + i);
            timeMap.put(i, new Date()); //Time of this Date is approximate, accuracy depends on how EvictionMap works
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
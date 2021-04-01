package com.atanava.evictionmap;

import com.atanava.evictionmap.helpers.TimingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
abstract class EvictionMapAbstractTest {

    private static final Logger log = LoggerFactory.getLogger("console");

    protected EvictionMapFactory<Integer, String> factory = new EvictionMapFactory<>();
    protected EvictionMap<Integer, String> evictionMap;
    protected long lifeTimeMillis = 2_000;
    protected int batchSize = 10_000_000;
    protected String expected = "Item ";
    Runtime runtime = Runtime.getRuntime();

    @Test
    void putAndGetOnce() throws InterruptedException {
        evictionMap.put(1, expected);
        assertEquals(expected, evictionMap.get(1));
        Thread.sleep(lifeTimeMillis);
        assertNull(evictionMap.get(1));
    }


    @Test
    void frequentPutAndGet() throws InterruptedException {
        long megaBytesBefore = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i, expected + i);
            evictionMap.get(i);
        }

        long megaBytesAfterPopulating = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        Thread.sleep(lifeTimeMillis);
        long megaBytesAfterEviction = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;

        for (int i = 0; i < batchSize; i++) {
            int j = i + batchSize;
            evictionMap.put(j, expected + j);

            assertNull(evictionMap.get(i));
            assertEquals(expected + j, evictionMap.get(j));
        }
        log.info('\n' + "megaBytesBefore = " + megaBytesBefore + '\n'
                + "megaBytesAfterPopulating = " + megaBytesAfterPopulating + '\n'
                + "megaBytesAfterEviction = " + megaBytesAfterEviction);
    }

    @Test
    void frequentPutAndRarelyGet() throws InterruptedException {
        long megaBytesBefore = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i, expected + i);
        }

        long megaBytesAfterPopulating = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        Thread.sleep(lifeTimeMillis);
        long megaBytesAfterEviction = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;

        for (int i = 0; i < batchSize; i++) {
            int j = i + batchSize;
            evictionMap.put(j, expected + j);

            assertNull(evictionMap.get(i));
            assertEquals(expected + j, evictionMap.get(j));
        }
        log.info('\n' + "megaBytesBefore = " + megaBytesBefore + '\n'
                + "megaBytesAfterPopulating = " + megaBytesAfterPopulating + '\n'
                + "megaBytesAfterEviction = " + megaBytesAfterEviction);
    }

    @Test
    void rarelyPutAndFrequentGet() throws InterruptedException {
        long megaBytesBefore = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        batchSize = batchSize/10;
        for (int i = 0; i < batchSize; i++) {
            evictionMap.put(i, expected + i);
            for (int j = 0; j < 100; j++) {
                evictionMap.get(i);
            }
        }

        long megaBytesAfterPopulating = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;
        Thread.sleep(lifeTimeMillis);
        long megaBytesAfterEviction = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576;

        for (int i = 0; i < batchSize; i++) {
            int j = i + batchSize;
            evictionMap.put(j, expected + j);

            assertNull(evictionMap.get(i));
            assertEquals(expected + j, evictionMap.get(j));
        }
        log.info('\n' + "megaBytesBefore = " + megaBytesBefore + '\n'
                + "megaBytesAfterPopulating = " + megaBytesAfterPopulating + '\n'
                + "megaBytesAfterEviction = " + megaBytesAfterEviction);
    }

}
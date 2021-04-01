package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;

class TheadSafeEvictionMapTest extends EvictionMapAbstractTest {

    @BeforeEach
    void init() {
        evictionMap = factory.getEvictionMap(MapType.THREAD_SAFE, lifeTimeMillis);
    }

}
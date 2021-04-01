package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;

class TheadUnsafeEvictionMapTest extends EvictionMapAbstractTest {

    @BeforeEach
    void init() {
        evictionMap = factory.getEvictionMap(MapType.THREAD_UNSAFE, lifeTimeMillis);
    }

}
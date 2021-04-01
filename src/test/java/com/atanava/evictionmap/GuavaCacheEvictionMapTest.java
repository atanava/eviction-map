package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;

class GuavaCacheEvictionMapTest extends EvictionMapAbstractTest {

    @BeforeEach
    void init() {
        evictionMap = factory.getEvictionMap(MapType.GUAVA, lifeTimeMillis);
    }

}
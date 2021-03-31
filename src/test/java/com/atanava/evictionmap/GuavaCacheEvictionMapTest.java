package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;

class GuavaCacheEvictionMapTest extends AbstractEvictionMapTest {

    @BeforeEach
    void init() {
        evictionMap = new GuavaCacheEvictionMap<>(lifeTimeMillis);
    }


}
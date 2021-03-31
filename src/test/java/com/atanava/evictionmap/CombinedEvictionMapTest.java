package com.atanava.evictionmap;

import org.junit.jupiter.api.BeforeEach;

class CombinedEvictionMapTest extends AbstractEvictionMapTest{

    @BeforeEach
    void init() {
        evictionMap = new CombinedEvictionMap<>(lifeTimeMillis, true, 1000);
    }

}
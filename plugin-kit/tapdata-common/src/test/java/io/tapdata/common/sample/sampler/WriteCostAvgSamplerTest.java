package io.tapdata.common.sample.sampler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WriteCostAvgSamplerTest {
    WriteCostAvgSampler sampler = new WriteCostAvgSampler();

    @Nested
    class valueTest {
        @BeforeEach
        void  setUp() {
            LongAdder counter = new LongAdder();
            counter.add(100);
            LongAdder totalValue = new LongAdder();
            totalValue.add(3000);
            ReflectionTestUtils.setField(sampler, "counter", counter);
            ReflectionTestUtils.setField(sampler, "totalValue", totalValue);
        }
        @Test
        void testNormal() {
            Number actual = sampler.value();
            assertEquals(30.0, actual);
        }

        @Test
        void testForReset() {
            AtomicLong lastCalculateTime = new AtomicLong(System.currentTimeMillis()-WriteCostAvgSampler.CALCULATE_PERIOD-10000);
            LongAdder lastTimeCounter = new LongAdder();
            lastTimeCounter.add(100);
            LongAdder lastTimeTotalValue = new LongAdder();
            lastTimeTotalValue.add(3000);
            ReflectionTestUtils.setField(sampler, "lastTimeCounter", lastTimeCounter);
            ReflectionTestUtils.setField(sampler, "lastTimeTotalValue", lastTimeTotalValue);
            ReflectionTestUtils.setField(sampler, "lastCalculateTime", lastCalculateTime);
            Number actual = sampler.value();
            assertNull(actual);
        }
    }
}

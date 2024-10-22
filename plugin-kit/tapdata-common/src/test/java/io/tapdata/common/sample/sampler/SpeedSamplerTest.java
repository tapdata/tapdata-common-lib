package io.tapdata.common.sample.sampler;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SpeedSamplerTest {

    @Test
    void valueTest() {
        SpeedSampler sampler = new SpeedSampler();
        assertNull(sampler.value());
        sampler.totalValue.add(1);
        sampler.lastCalculateTime = System.currentTimeMillis() - 1000;
        Number value = sampler.value();
        assertNotNull(value);
        assertTrue(value.intValue() > 0);
        for (int i = 0; i < 10; i++) {
            sampler.lastCalculateTime = System.currentTimeMillis() - 1000;
            sampler.value();
        }
        assertTrue(sampler.maxValue > 0);
        assertEquals(0, sampler.valueList.size());
    }

    @Test
    void getAvgValueTest() {
        SpeedSampler sampler = new SpeedSampler();
        assertEquals(Double.NaN, sampler.getAvgValue());
        List<Double> values = new ArrayList<>();
        values.add(1.0);
        values.add(2.0);
        values.add(3.0);

        sampler.valueList.addAll(values);

        double expectedAvg = 2.0;
        assertEquals(expectedAvg, sampler.getAvgValue());
        sampler.valueList.clear();
        sampler.valueList.add(5.0);

        expectedAvg = 5.0;
        assertEquals(expectedAvg, sampler.getAvgValue());
    }
}

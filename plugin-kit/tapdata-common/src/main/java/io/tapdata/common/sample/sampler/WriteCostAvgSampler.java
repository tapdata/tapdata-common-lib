package io.tapdata.common.sample.sampler;

import io.tapdata.common.sample.SamplerPrometheus;
import io.tapdata.firedome.MultiTaggedGauge;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Incremental record value and auto inc counter.
 * Calculate average value and clear when upload.
 */
public class WriteCostAvgSampler implements SamplerPrometheus {
    public static final int CALCULATE_PERIOD = 300000;
    private final LongAdder counter = new LongAdder();
    private final LongAdder totalValue = new LongAdder();

    private final AtomicLong writeRecordAcceptLastTs = new AtomicLong();
    private String[] tagValues;
    private MultiTaggedGauge multiTaggedGauge;

    public WriteCostAvgSampler() {
    }

    public WriteCostAvgSampler(MultiTaggedGauge multiTaggedGauge, String[] tagValues) {
        this.multiTaggedGauge = multiTaggedGauge;
        this.tagValues = tagValues;
    }

    private final LongAdder lastTimeCounter = new LongAdder();
    private final LongAdder lastTimeTotalValue = new LongAdder();
    private final AtomicLong lastCalculateTime = new AtomicLong(System.currentTimeMillis());

    public void setWriteRecordAcceptLastTs(long ts) {
        writeRecordAcceptLastTs.set(ts);
    }

    public void add(long value) {
        counter.increment();
        totalValue.add(value);
    }

    public void add(long cnt, long accetTime) {
        counter.add(cnt);
        totalValue.add(accetTime - writeRecordAcceptLastTs.getAndSet(accetTime));
    }

    @Override
    public Number value() {
        long counterValue = counter.sum();
        double total = totalValue.sum();
        long currentTime = System.currentTimeMillis();

        boolean shouldReset = (currentTime - lastCalculateTime.get() > CALCULATE_PERIOD)
                && (counterValue == lastTimeCounter.sum())
                && (total == lastTimeTotalValue.sum());
        if (shouldReset) {
            lastCalculateTime.set(currentTime);
            counter.reset();
            totalValue.reset();
            return null;
        }
        lastTimeCounter.reset();
        lastTimeTotalValue.reset();
        lastTimeCounter.add(counterValue);
        lastTimeTotalValue.add(totalValue.sum());
        if(counterValue > 0) {
            return total / counterValue;
        }
        return null;
    }

    @Override
    public MultiTaggedGauge multiTaggedGauge() {
        return multiTaggedGauge;
    }

    @Override
    public String[] tagValues() {
        return tagValues;
    }
}

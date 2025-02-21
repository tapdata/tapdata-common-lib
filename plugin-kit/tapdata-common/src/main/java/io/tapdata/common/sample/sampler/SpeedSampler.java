package io.tapdata.common.sample.sampler;

import com.google.common.collect.Lists;
import io.tapdata.common.sample.Sampler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * Incremental record value and last upload time.
 * Calculate speed by using current time minus last upload time and update last upload time when upload.
 */
public class SpeedSampler implements Sampler {
    private static final int BUFFER_LIMIT = 10;
    protected Long lastCalculateTime;
    protected final LongAdder totalValue = new LongAdder();
    protected Double maxValue;
    protected final List<Double> valueList = Lists.newArrayList();
    private BigDecimal totalSamples = BigDecimal.ZERO;
    private BigDecimal totalSampleCount = BigDecimal.ZERO;
    public void add(long value) {
        totalValue.add(value);
    }

    public void add() {
        totalValue.add(1);
    }

    @Override
    public Number value() {
        Long temp = lastCalculateTime;
        lastCalculateTime = System.currentTimeMillis();
        if(temp != null) {
            long time = System.currentTimeMillis() - temp;
            if(time > 0) {
                double v = ((double) totalValue.sumThenReset() / time) * 1000;

                if (valueList.size() == BUFFER_LIMIT) {
                    totalSamples = totalSamples.add(BigDecimal.valueOf(valueList.stream().mapToDouble(value -> value).sum() + v));
                    totalSampleCount = totalSampleCount.add(BigDecimal.valueOf(valueList.size() + 1));
                    valueList.clear();
                } else {
                    valueList.add(v);
                }

                if (Objects.isNull(maxValue) || v > maxValue) {
                    maxValue = v;
                }
                return v;
            }
        }
        return null;
    }

    public Double getMaxValue() {
        if (Objects.nonNull(maxValue)) {
            return maxValue;
        }
        return null;
    }

    public Double getAvgValue() {
        BigDecimal total = totalSamples;
        BigDecimal count = totalSampleCount;
        if (!valueList.isEmpty()) {
            total = total.add(BigDecimal.valueOf(valueList.stream().mapToDouble(v -> v).sum()));
            count = count.add(BigDecimal.valueOf(valueList.size()));
        }
        if (count.equals(BigDecimal.ZERO)) {
            return Double.NaN;
        }
        return total.divide(count, 2, RoundingMode.HALF_UP).doubleValue();
    }
}
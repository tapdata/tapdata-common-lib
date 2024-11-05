package io.tapdata.pdk.core.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/11/4 09:03
 */
public class SampleRetryLifeCycle implements RetryLifeCycle {

    protected AtomicLong totalRetries = new AtomicLong(0);
    protected AtomicLong retryTimes = new AtomicLong();
    protected boolean async = false;
    protected AtomicLong startRetryTs = new AtomicLong(0);
    protected AtomicLong endRetryTs = new AtomicLong(0);
    protected long lastRetryTime = 0;
    protected long retryPeriod;
    private TimeUnit timeUnit;
    protected String retryOp;
    protected Boolean success;

    @Override
    public void startRetry(long retryTimes, boolean async, long retryPeriod, TimeUnit timeUnit, String retryOp) {

        if (totalRetries.get() == 0)
            totalRetries.compareAndSet(0, retryTimes);

        this.retryTimes.incrementAndGet();
        this.async = async;

        if (startRetryTs.get() == 0)
            startRetryTs.compareAndSet(0, System.currentTimeMillis());

        this.lastRetryTime = System.currentTimeMillis();
        this.retryPeriod = retryPeriod;
        this.timeUnit = timeUnit;
        this.retryOp = retryOp;

        onChange();
    }

    public Long getNextRetryTimestamp() {
        return lastRetryTime + getRetryPeriodOfMillisecond();
    }

    public Long getRetryPeriodOfMillisecond() {
        if (retryPeriod > 0 && timeUnit != null) {
            switch (timeUnit) {
                case NANOSECONDS:
                    return retryPeriod / 1000 / 1000;
                case MICROSECONDS:
                    return retryPeriod / 1000;
                case MILLISECONDS:
                    return retryPeriod;
                case SECONDS:
                    return retryPeriod * 1000;
                case MINUTES:
                    return retryPeriod * 60000;
                case HOURS:
                    return retryPeriod * 3600000;
                case DAYS:
                    return retryPeriod * 24 * 3600000;
            }
        }
        return 0L;
    }

    @Override
    public void exceededRetries(long retryTimes) {
        endRetryTs.set(System.currentTimeMillis());
        success = false;
        onChange();
    }

    @Override
    public void success() {
        endRetryTs.set(System.currentTimeMillis());
        success = true;
        onChange();
    }
}

package io.tapdata.pdk.core.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/11/4 09:03
 */
class EmptyRetryLifeCycle implements RetryLifeCycle {


    @Override
    public void startRetry(long retryTimes, boolean async, long retryPeriod, TimeUnit timeUnit, String retryOp) {
    }

    @Override
    public void exceededRetries(long retryTimes) {
    }

    @Override
    public void success() {
    }
}

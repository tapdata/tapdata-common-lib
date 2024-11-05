package io.tapdata.pdk.core.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/10/28 17:32
 */
public interface RetryLifeCycle {
    void startRetry(long retryTimes, boolean async, long retryPeriod, TimeUnit timeUnit, String retryOp);
    void exceededRetries(long retryTimes);
    void success();

    default void onChange() {

    }
}

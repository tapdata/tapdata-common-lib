package io.tapdata.pdk.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/11/4 11:10
 */
public class RetryUtilsTest {

    @Test
    void testNonRetryLifeCycle() {

    }

    @Test
    void testSampleRetryLifeCycle() {
        SampleRetryLifeCycle retryLifeCycle = new SampleRetryLifeCycle();

        Assertions.assertDoesNotThrow(() -> {

            Assertions.assertEquals(0L, retryLifeCycle.getNextRetryTimestamp());
        });

        retryLifeCycle.startRetry(10, false, 10, TimeUnit.SECONDS, "WRITE");

        Assertions.assertEquals(10, retryLifeCycle.totalRetries.get());
        Assertions.assertFalse(retryLifeCycle.async);
        Assertions.assertEquals("WRITE", retryLifeCycle.retryOp);
        Assertions.assertNotNull(retryLifeCycle.startRetryTs);
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 10 * 1000, retryLifeCycle.getNextRetryTimestamp());

        long start = retryLifeCycle.startRetryTs.get();

        retryLifeCycle.startRetry(15, true, 1, TimeUnit.MINUTES, "READ");

        Assertions.assertEquals(start, retryLifeCycle.startRetryTs.get());
        Assertions.assertTrue(retryLifeCycle.async);
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 60 * 1000, retryLifeCycle.getNextRetryTimestamp());

        retryLifeCycle.exceededRetries(10);
        Assertions.assertTrue(retryLifeCycle.endRetryTs.get() > 0);
        Assertions.assertFalse(retryLifeCycle.success);

        retryLifeCycle.success();
        Assertions.assertTrue(retryLifeCycle.endRetryTs.get() > 0);
        Assertions.assertTrue(retryLifeCycle.success);

        retryLifeCycle.startRetry(15, true, 1000000000, TimeUnit.NANOSECONDS, "READ");
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 1000, retryLifeCycle.getNextRetryTimestamp());

        retryLifeCycle.startRetry(15, true, 1000000, TimeUnit.MICROSECONDS, "READ");
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 1000, retryLifeCycle.getNextRetryTimestamp());

        retryLifeCycle.startRetry(15, true, 1000, TimeUnit.MILLISECONDS, "READ");
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 1000, retryLifeCycle.getNextRetryTimestamp());

        retryLifeCycle.startRetry(15, true, 1, TimeUnit.HOURS, "READ");
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 1000 * 60 * 60, retryLifeCycle.getNextRetryTimestamp());

        retryLifeCycle.startRetry(15, true, 1, TimeUnit.DAYS, "READ");
        Assertions.assertEquals(retryLifeCycle.lastRetryTime + 1000 * 60 * 60 * 24, retryLifeCycle.getNextRetryTimestamp());
    }

}

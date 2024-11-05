package io.tapdata.pdk.core.entity.params;

import io.tapdata.pdk.core.utils.RetryLifeCycle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/11/1 12:13
 */
public class PDKMethodInvokerTest {

    @Test
    void testRetryLifeCycle() {
        RetryLifeCycle retryLifeCycle = mock(RetryLifeCycle.class);
        PDKMethodInvoker pdkMethodInvoker = PDKMethodInvoker.create().retryLifeCycle(retryLifeCycle);

        Assertions.assertEquals(retryLifeCycle, pdkMethodInvoker.getRetryLifeCycle());
        pdkMethodInvoker.setRetryLifeCycle(retryLifeCycle);
        Assertions.assertEquals(retryLifeCycle, pdkMethodInvoker.getRetryLifeCycle());
    }

}

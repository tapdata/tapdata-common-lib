package io.tapdata.pdk.core.utils;

import io.tapdata.exception.TapCodeException;
import io.tapdata.pdk.apis.context.TapConnectionContext;
import io.tapdata.pdk.apis.functions.PDKMethod;
import io.tapdata.pdk.apis.functions.connection.ErrorHandleFunction;
import io.tapdata.pdk.core.error.TapPdkRunnerExCode_18;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2024/11/4 11:10
 */
public class RetryUtilsTest {

    @Test
    void testNonRetryLifeCycle() {

    }

    @Nested
    class callErrorHandleFunctionIfNeedTest {
        PDKMethod method;
        String message;
        Throwable errThrowable;
        ErrorHandleFunction function;
        TapConnectionContext tapConnectionContext;
        @Test
        @DisplayName("test for CALL_ERROR_HANDLE_API_ERROR")
        void test1() throws Throwable {
            method = mock(PDKMethod.class);
            message = "test for CALL_ERROR_HANDLE_API_ERROR";
            errThrowable = new RuntimeException("test for CALL_ERROR_HANDLE_API_ERROR");
            function = mock(ErrorHandleFunction.class);
            tapConnectionContext = mock(TapConnectionContext.class);
            when(function.needRetry(tapConnectionContext, method, errThrowable)).thenThrow(RuntimeException.class);
            TapCodeException exception = assertThrows(TapCodeException.class, () -> RetryUtils.callErrorHandleFunctionIfNeed(method, message, errThrowable, function, tapConnectionContext));
            assertEquals(TapPdkRunnerExCode_18.CALL_ERROR_HANDLE_API_ERROR, exception.getCode());
        }
    }
}

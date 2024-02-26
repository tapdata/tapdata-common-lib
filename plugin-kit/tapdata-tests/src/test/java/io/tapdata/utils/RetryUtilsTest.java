package io.tapdata.utils;

import io.tapdata.ErrorCodeConfig;
import io.tapdata.ErrorCodeEntity;
import io.tapdata.exception.TapCodeException;
import io.tapdata.pdk.apis.functions.PDKMethod;
import io.tapdata.pdk.apis.functions.connection.RetryOptions;
import io.tapdata.pdk.core.entity.params.PDKMethodInvoker;
import io.tapdata.pdk.core.utils.CommonUtils;
import io.tapdata.pdk.core.utils.RetryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

public class RetryUtilsTest {
    @Nested
    @DisplayName("autoRetry method with param method and invoker test")
    class autoRetryWithMethodAndInvokerTest{
        @Test
        @DisplayName("invoker retry time less than 0")
        void test1() throws Throwable {
            PDKMethod method = PDKMethod.IENGINE_FIND_SCHEMA;
            PDKMethodInvoker invoker = mock(PDKMethodInvoker.class);
            CommonUtils.AnyError runnable = mock(CommonUtils.AnyError.class);
            when(invoker.getR()).thenReturn(runnable);
            when(invoker.getRetryTimes()).thenReturn(-1L);
            RetryUtils.autoRetry(method, invoker);
            verify(runnable, new Times(0)).run();
        }
        @Test
        @DisplayName("retry with exception")
        void test2() throws Throwable {
            Exception e = new Exception();
            PDKMethod method = PDKMethod.IENGINE_FIND_SCHEMA;
            PDKMethodInvoker invoker = mock(PDKMethodInvoker.class);
            CommonUtils.AnyError runnable = mock(CommonUtils.AnyError.class);
            when(invoker.getR()).thenReturn(runnable);
            when(invoker.getRetryTimes()).thenReturn(1L);
            doThrow(new Exception()).when(runnable).run();
            assertThrows(Exception.class,()->RetryUtils.autoRetry(method, invoker));
        }
    }
    @Nested
    @DisplayName("callErrorHandleFunctionIfNeed method with param throwable test")
    class CallErrorHandleFunctionIfNeedTest{
        @Test
        @DisplayName("test with TapCodeException")
        void test1(){
            ErrorCodeConfig config = mock(ErrorCodeConfig.class);
            try (MockedStatic<ErrorCodeConfig> mb = Mockito
                    .mockStatic(ErrorCodeConfig.class)) {
                mb.when(ErrorCodeConfig::getInstance).thenReturn(config);
                TapCodeException throwable = mock(TapCodeException.class);
                when(throwable.getCode()).thenReturn("111");
                ErrorCodeEntity errorCode = mock(ErrorCodeEntity.class);
                when(config.getErrorCode("111")).thenReturn(errorCode);
                when(errorCode.isRecoverable()).thenReturn(true);
                RetryOptions actual = RetryUtils.callErrorHandleFunctionIfNeed(throwable);
                assertEquals(true, actual.isNeedRetry());
            }
        }
        @Test
        @DisplayName("test with Exception")
        void test2(){
            ErrorCodeConfig config = mock(ErrorCodeConfig.class);
            try (MockedStatic<ErrorCodeConfig> mb = Mockito
                    .mockStatic(ErrorCodeConfig.class)) {
                mb.when(ErrorCodeConfig::getInstance).thenReturn(config);
                Exception throwable = mock(Exception.class);
                ErrorCodeEntity errorCode = mock(ErrorCodeEntity.class);
                when(config.getErrorCode("111")).thenReturn(errorCode);
                when(errorCode.isRecoverable()).thenReturn(true);
                RetryOptions actual = RetryUtils.callErrorHandleFunctionIfNeed(throwable);
                assertEquals(false, actual.isNeedRetry());
            }
        }
    }
    @Nested
    class FunctionRetryFlagTest {
        @DisplayName("need retry exception, retry succeed")
        @Test
        void test1() {
            PDKMethod pdkMethod = PDKMethod.TARGET_WRITE_RECORD;
            AtomicInteger i = new AtomicInteger();
            CommonUtils.AnyError runnable = () -> {
                if (i.getAndIncrement() == 0) {
                    throw new IOException();
                }
            };
            Runnable signFunction = mock(Runnable.class);
            Runnable cleanFunction = mock(Runnable.class);
            PDKMethodInvoker invoker = PDKMethodInvoker.create().runnable(runnable)
                    .signFunctionRetry(signFunction)
                    .cleanFunctionRetry(cleanFunction)
                    .retryPeriodSeconds(5L)
                    .retryTimes(1L);
            RetryUtils.autoRetry(pdkMethod, invoker);
            verify(signFunction, times(1)).run();
            verify(cleanFunction, times(1)).run();
        }

        @DisplayName("don't need retry exception,throw exception")
        @Test
        void test2() throws Throwable {
            PDKMethod pdkMethod = PDKMethod.TARGET_WRITE_RECORD;
            CommonUtils.AnyError runnable = mock(CommonUtils.AnyError.class);
            Runnable signFunction = mock(Runnable.class);
            Runnable cleanFunction = mock(Runnable.class);
            PDKMethodInvoker invoker = PDKMethodInvoker.create().runnable(runnable)
                    .signFunctionRetry(signFunction)
                    .cleanFunctionRetry(cleanFunction)
                    .retryPeriodSeconds(5L)
                    .retryTimes(1L);
            doThrow(new Exception()).when(runnable).run();
            assertThrows(Exception.class, () -> {
                RetryUtils.autoRetry(pdkMethod, invoker);
            });
        }

        @DisplayName("need retry exception, no retry succeed")
        @Test
        void test3() throws Throwable {
            PDKMethod pdkMethod = PDKMethod.TARGET_WRITE_RECORD;
            CommonUtils.AnyError runnable = mock(CommonUtils.AnyError.class);
            Runnable signFunction = mock(Runnable.class);
            Runnable cleanFunction = mock(Runnable.class);
            PDKMethodInvoker invoker = PDKMethodInvoker.create().runnable(runnable)
                    .signFunctionRetry(signFunction)
                    .cleanFunctionRetry(cleanFunction)
                    .retryPeriodSeconds(5L)
                    .retryTimes(1L);
            doThrow(new IOException()).when(runnable).run();
            assertThrows(Exception.class, () -> {
                RetryUtils.autoRetry(pdkMethod, invoker);
            });
            verify(signFunction, times(1)).run();
            verify(cleanFunction, times(1)).run();
        }

    }
}

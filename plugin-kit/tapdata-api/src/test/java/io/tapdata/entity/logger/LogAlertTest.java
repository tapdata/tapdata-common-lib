package io.tapdata.entity.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class LogAlertTest {

    @AfterEach
    void tearDown() {
        TapLogger.setLogListener(null);
    }

    @Test
    void defaultLogAlertShouldDegradeToError() {
        AtomicInteger errorCount = new AtomicInteger();
        Log log = new Log() {
            @Override
            public void debug(String message, Object... params) {
            }

            @Override
            public void info(String message, Object... params) {
            }

            @Override
            public void trace(String message, Object... params) {
            }

            @Override
            public void warn(String message, Object... params) {
            }

            @Override
            public void error(String message, Object... params) {
                errorCount.incrementAndGet();
            }

            @Override
            public void error(String message, Throwable throwable) {
                errorCount.incrementAndGet();
            }

            @Override
            public void fatal(String message, Object... params) {
            }
        };

        log.alert("discarded {}", "t1");
        log.alert("discarded {}", new RuntimeException("cause"));
        Assertions.assertEquals(2, errorCount.get());
    }

    @Test
    void tapLogAlertShouldCallListenerAlertInsteadOfError() {
        List<String> errors = new ArrayList<>();
        List<String> alerts = new ArrayList<>();
        TapLogger.setLogListener(new TapLogger.LogListener() {
            @Override
            public void debug(String log) {
            }

            @Override
            public void info(String log) {
            }

            @Override
            public void warn(String log) {
            }

            @Override
            public void error(String log) {
                errors.add(log);
            }

            @Override
            public void fatal(String log) {
            }

            @Override
            public void memory(String memoryLog) {
            }

            @Override
            public void alert(String log) {
                alerts.add(log);
            }
        });

        new TapLog().alert("CDC event for table '{}' cannot be decoded safely", "CFPCN");

        Assertions.assertEquals(1, alerts.size());
        Assertions.assertTrue(alerts.get(0).contains("CFPCN"));
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void oldListenerWithoutAlertOverrideShouldFallBackToError() {
        List<String> errors = new ArrayList<>();
        TapLogger.setLogListener(new TapLogger.LogListener() {
            @Override
            public void debug(String log) {
            }

            @Override
            public void info(String log) {
            }

            @Override
            public void warn(String log) {
            }

            @Override
            public void error(String log) {
                errors.add(log);
            }

            @Override
            public void fatal(String log) {
            }

            @Override
            public void memory(String memoryLog) {
            }
        });

        new TapLog().alert("legacy listener {}", "ok");
        Assertions.assertEquals(1, errors.size());
    }
}

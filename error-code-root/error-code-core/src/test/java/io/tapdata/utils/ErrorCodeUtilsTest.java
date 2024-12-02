package io.tapdata.utils;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class ErrorCodeUtilsTest {
    @Test
    public void testTruncateData() {
        StringBuilder dataBuilder = new StringBuilder();
        for (int i = 0; i < 10 * 1024 * 1024; i++) {
            dataBuilder.append("A");  // 填充大量数据
        }
        String largeData = dataBuilder.toString();

        String result = ErrorCodeUtils.truncateData(largeData);

        int length = result.getBytes(StandardCharsets.UTF_8).length;
        System.out.println(length);
        assertTrue(length <= 1024 * 1024 + 3);
    }
}

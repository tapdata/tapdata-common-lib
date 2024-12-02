package io.tapdata.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ErrorCodeUtils {
    private static final int MAX_SIZE_IN_BYTES = 1 * 1024 * 1024;

    /**
     * 截取数据，当内容超过 1MB 时省略后面的内容
     * @param data 输入数据
     * @return 截取后的数据
     */
    public static String truncateData(Object data) {
        if (null == data) return StringUtils.EMPTY;
        String dataString = data.toString();
        if (StringUtils.isEmpty(dataString)) {
            return dataString;
        }
        byte[] dataBytes = dataString.getBytes(StandardCharsets.UTF_8);
        if (dataBytes.length > MAX_SIZE_IN_BYTES) {
            byte[] truncatedData = new byte[MAX_SIZE_IN_BYTES];
            System.arraycopy(dataBytes, 0, truncatedData, 0, MAX_SIZE_IN_BYTES);
            return new String(truncatedData, StandardCharsets.UTF_8) + "...";
        }
        return dataString;
    }
}

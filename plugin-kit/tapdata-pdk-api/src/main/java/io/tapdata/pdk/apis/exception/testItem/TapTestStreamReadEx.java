package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestStreamReadEx extends TapTestItemException {
    public static final String STREAM_READ_FAIL = "stream.read.fail";
    public static final String STREAM_READ_SOLUTION = "stream.read.solution";
    public static final String STREAM_READ_REASON = "stream.read.reason";

    public TapTestStreamReadEx(Throwable cause) {
        super(cause, STREAM_READ_FAIL, STREAM_READ_REASON, STREAM_READ_SOLUTION);
    }
    public TapTestStreamReadEx(String message, Throwable cause) {
        super(message, cause);
    }
}

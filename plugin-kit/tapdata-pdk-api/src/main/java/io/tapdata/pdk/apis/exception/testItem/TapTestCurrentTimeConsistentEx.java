package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestCurrentTimeConsistentEx extends TapTestItemException {
    public static final String TIME_CONSISTENT_FAIL = "time.consistent.fail";
    public static final String TIME_CONSISTENT_SOLUTION = "time.consistent.solution";
    public static final String TIME_CONSISTENT_REASON = "time.consistent.reason";

    public TapTestCurrentTimeConsistentEx(Throwable cause) {
        super(cause);
        buildMessage();
    }

    public TapTestCurrentTimeConsistentEx(String message, Throwable cause) {
        super(message, cause);
    }

    public void buildMessage() {
        buildMessage(TIME_CONSISTENT_FAIL, TIME_CONSISTENT_REASON, TIME_CONSISTENT_SOLUTION);
    }
}

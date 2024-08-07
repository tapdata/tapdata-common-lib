package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestAuthEx extends TapTestItemException {

    public static final String PDK_AUTH_FAIL = "pdk.auth.fail";
    public static final String PDK_AUTH_SOLUTION = "pdk.auth.solution";
    public static final String PDK_AUTH_REASON = "pdk.auth.reason";
    public TapTestAuthEx(Throwable cause) {
        super(cause);
        buildMessage();
    }

    public TapTestAuthEx(String message, Throwable cause) {
        super(message, cause);
    }
    public void buildMessage() {
        buildMessage(PDK_AUTH_FAIL, PDK_AUTH_REASON, PDK_AUTH_SOLUTION);
    }
}

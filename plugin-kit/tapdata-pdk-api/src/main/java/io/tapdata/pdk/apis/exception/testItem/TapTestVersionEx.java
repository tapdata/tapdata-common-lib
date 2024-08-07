package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestVersionEx extends TapTestItemException {
    private String solution = "Ensure current user has enough privilege to query";
    public static final String PDK_VERSION_FAIL = "pdk.version.fail";
    public static final String PDK_VERSION_SOLUTION = "pdk.version.solution";
    public static final String PDK_VERSION_REASON = "pdk.version.reason";

    public TapTestVersionEx(Throwable cause) {
        super(cause);
        buildMessage();
    }

    public TapTestVersionEx(String message, Throwable cause) {
        super(message, cause);
    }

    public void buildMessage() {
        buildMessage(PDK_VERSION_FAIL, PDK_VERSION_REASON, PDK_VERSION_SOLUTION);
    }
}

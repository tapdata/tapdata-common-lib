package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestVersionEx extends TapTestItemException {
    public static final String PDK_VERSION_FAIL = "pdk.version.fail";
    public static final String PDK_VERSION_SOLUTION = "pdk.version.solution";
    public static final String PDK_VERSION_REASON = "pdk.version.reason";

    public TapTestVersionEx(Throwable cause) {
        super(cause, PDK_VERSION_FAIL, PDK_VERSION_REASON, PDK_VERSION_SOLUTION);
    }

    public TapTestVersionEx(String message, Throwable cause) {
        super(message, cause);
    }
}

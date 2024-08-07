package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestUnknownEx extends TapTestItemException {
    private String solution = "1. Ensure database server is available;\n2. Report the issue to Tapdata";

    public static final String PDK_UNKNOWN_FAIL = "pdk.unknown.fail";
    public static final String PDK_UNKNOWN_SOLUTION = "pdk.unknown.solution";
    public static final String PDK_UNKNOWN_REASON = "pdk.unknown.reason";

    public TapTestUnknownEx(Throwable cause) {
        super(cause, PDK_UNKNOWN_FAIL, PDK_UNKNOWN_REASON, PDK_UNKNOWN_SOLUTION);
    }
    public TapTestUnknownEx(String message, Throwable cause) {
        super(message, cause);
    }
}

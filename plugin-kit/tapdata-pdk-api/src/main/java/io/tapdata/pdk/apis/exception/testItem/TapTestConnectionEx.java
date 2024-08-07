package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestConnectionEx extends TapTestItemException {
    public static final String PDK_CONNECTION_FAIL = "pdk.connection.fail";
    public static final String PDK_CONNECTION_SOLUTION = "pdk.connection.solution";
    public static final String PDK_CONNECTION_REASON = "pdk.connection.reason";


    public TapTestConnectionEx(Throwable cause) {
        super(cause);
        buildMessage();
    }

    public TapTestConnectionEx(String message, Throwable cause) {
        super(message, cause);
    }

    public void buildMessage() {
        buildMessage(PDK_CONNECTION_FAIL, PDK_CONNECTION_REASON, PDK_CONNECTION_SOLUTION);
    }
}

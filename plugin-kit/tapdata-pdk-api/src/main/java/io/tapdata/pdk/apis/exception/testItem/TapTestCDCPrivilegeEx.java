package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestCDCPrivilegeEx extends TapTestItemException {
    public static final String CHECK_CEC_PRIVILEGE_FAIL = "check.cdc.privilege.fail";
    public static final String CHECK_CEC_PRIVILEGE_SOLUTION = "check.cdc.privilege.solution";
    public static final String CHECK_CEC_PRIVILEGE_REASON = "check.cdc.privilege.reason";
    public TapTestCDCPrivilegeEx(Throwable cause) {
        super(cause, CHECK_CEC_PRIVILEGE_FAIL, CHECK_CEC_PRIVILEGE_REASON, CHECK_CEC_PRIVILEGE_SOLUTION);
    }

    public TapTestCDCPrivilegeEx(String message, Throwable cause) {
        super(message, cause);
    }
}

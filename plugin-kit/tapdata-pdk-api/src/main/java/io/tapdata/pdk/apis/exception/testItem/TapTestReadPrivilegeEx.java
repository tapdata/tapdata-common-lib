package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestReadPrivilegeEx extends TapTestItemException {
    public static final String READ_PRIVILEGE_FAIL = "read.privilege.fail";
    public static final String READ_PRIVILEGE_SOLUTION = "read.privilege.solution";
    public static final String READ_PRIVILEGE_REASON = "read.privilege.reason";

    public TapTestReadPrivilegeEx(Throwable cause) {
        super(cause, READ_PRIVILEGE_FAIL, READ_PRIVILEGE_REASON, READ_PRIVILEGE_SOLUTION);
    }

    public TapTestReadPrivilegeEx(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestWritePrivilegeEx extends TapTestItemException {
    public static final String WRITE_PRIVILEGE_FAIL = "write.privilege.fail";
    public static final String WRITE_PRIVILEGE_SOLUTION = "write.privilege.solution";
    public static final String WRITE_PRIVILEGE_REASON = "write.privilege.reason";

    public TapTestWritePrivilegeEx(Throwable cause) {
        super(cause, WRITE_PRIVILEGE_FAIL, WRITE_PRIVILEGE_REASON, WRITE_PRIVILEGE_SOLUTION);
    }

    public TapTestWritePrivilegeEx(String message, Throwable cause) {
        super(message, cause);
    }
}

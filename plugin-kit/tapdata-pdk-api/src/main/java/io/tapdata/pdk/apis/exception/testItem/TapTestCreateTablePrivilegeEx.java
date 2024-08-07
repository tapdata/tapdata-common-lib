package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestCreateTablePrivilegeEx extends TapTestItemException {
    public static final String CREATE_TABLE_PRIVILEGE_FAIL = "create.table.privilege.fail";
    public static final String CREATE_TABLE_PRIVILEGE_SOLUTION = "create.table.privilege.solution";
    public static final String CREATE_TABLE_PRIVILEGE_REASON = "create.table.privilege.reason";
    public TapTestCreateTablePrivilegeEx(Throwable cause) {
        super(cause, CREATE_TABLE_PRIVILEGE_FAIL, CREATE_TABLE_PRIVILEGE_REASON, CREATE_TABLE_PRIVILEGE_SOLUTION);
    }

    public TapTestCreateTablePrivilegeEx(String message, Throwable cause) {
        super(message, cause);
    }
}

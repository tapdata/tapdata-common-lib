package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * One DDL statement of a {@link TriggerCdcInstallPlan}.
 *
 * <p>The plan is also the MANUAL install artefact: a DBA executes exactly these statements.
 * Therefore the statements must be complete, ordered, and identical to what
 * {@code ensure} would run.</p>
 */
public class TriggerCdcStatement implements Serializable {
    private static final long serialVersionUID = 1L;

    protected TriggerCdcStatementType type;
    protected String sql;
    /** What the statement is for, shown in the UI and in the generated install script. */
    protected String description;
    /** True when re-running the statement is harmless. */
    protected boolean idempotent;
    /** True when the statement locks a customer table, so the UI can warn about it. */
    protected boolean mayLockSourceTable;
    /** Privileges a DBA needs for this statement, for example "CREATE TRIGGER". */
    protected List<String> requiredPrivileges;

    public static TriggerCdcStatement create() {
        return new TriggerCdcStatement();
    }

    public TriggerCdcStatement privilege(String privilege) {
        if (requiredPrivileges == null) {
            requiredPrivileges = new ArrayList<>();
        }
        requiredPrivileges.add(privilege);
        return this;
    }

    public TriggerCdcStatementType getType() {
        return type;
    }

    public void setType(TriggerCdcStatementType type) {
        this.type = type;
    }

    public TriggerCdcStatement type(TriggerCdcStatementType type) {
        this.type = type;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public TriggerCdcStatement sql(String sql) {
        this.sql = sql;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TriggerCdcStatement description(String description) {
        this.description = description;
        return this;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public TriggerCdcStatement idempotent(boolean idempotent) {
        this.idempotent = idempotent;
        return this;
    }

    public boolean isMayLockSourceTable() {
        return mayLockSourceTable;
    }

    public void setMayLockSourceTable(boolean mayLockSourceTable) {
        this.mayLockSourceTable = mayLockSourceTable;
    }

    public TriggerCdcStatement mayLockSourceTable(boolean mayLockSourceTable) {
        this.mayLockSourceTable = mayLockSourceTable;
        return this;
    }

    public List<String> getRequiredPrivileges() {
        return requiredPrivileges;
    }

    public void setRequiredPrivileges(List<String> requiredPrivileges) {
        this.requiredPrivileges = requiredPrivileges;
    }
}

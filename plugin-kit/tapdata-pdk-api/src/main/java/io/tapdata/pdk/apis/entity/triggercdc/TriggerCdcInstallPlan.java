package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Read only result of {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcPlanFunction}.
 *
 * <p>A plan must be side effect free: nothing is created, so the UI can show the cost, the
 * privileges and the blocking problems before the operator commits.</p>
 */
public class TriggerCdcInstallPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String catalog;
    protected String schema;
    /** Objects that will exist after installation. */
    protected List<TriggerCdcObject> objects;
    /**
     * Ordered statements to execute. A plan without statements can never be installed — in
     * {@code AUTO} nothing would be created, in {@code MANUAL} the DBA would have nothing to run —
     * so an empty list always blocks the plan and must be reported through
     * {@link #isBlocked()} with a reason in {@link #getBlockedReason()}.
     */
    protected List<TriggerCdcStatement> statements;
    /** Tables the connector accepts into this capture. */
    protected List<TriggerCdcTable> supportedTables;
    /**
     * Tables the connector refuses, with the reason inside
     * {@link TriggerCdcTableResult#getMessage()}. A blocked table fails the plan instead of
     * being silently skipped, because a silently skipped table is a silent data gap.
     */
    protected List<TriggerCdcTableResult> blockedTables;
    /** Non blocking remarks: cost expectation, high update rate warnings, existing triggers. */
    protected List<String> warnings;
    /** True when installation must not proceed. */
    protected boolean blocked;
    /** Why the plan is blocked, when {@link #blocked} is true. */
    protected String blockedReason;

    public static TriggerCdcInstallPlan create() {
        return new TriggerCdcInstallPlan();
    }

    public TriggerCdcInstallPlan object(TriggerCdcObject object) {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.add(object);
        return this;
    }

    public TriggerCdcInstallPlan statement(TriggerCdcStatement statement) {
        if (statements == null) {
            statements = new ArrayList<>();
        }
        statements.add(statement);
        return this;
    }

    public TriggerCdcInstallPlan warning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
        return this;
    }

    public TriggerCdcInstallPlan supportedTable(TriggerCdcTable table) {
        if (supportedTables == null) {
            supportedTables = new ArrayList<>();
        }
        supportedTables.add(table);
        return this;
    }

    public TriggerCdcInstallPlan blockedTable(TriggerCdcTableResult result) {
        if (blockedTables == null) {
            blockedTables = new ArrayList<>();
        }
        blockedTables.add(result);
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public TriggerCdcInstallPlan catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcInstallPlan schema(String schema) {
        this.schema = schema;
        return this;
    }

    public List<TriggerCdcObject> getObjects() {
        return objects;
    }

    public void setObjects(List<TriggerCdcObject> objects) {
        this.objects = objects;
    }

    public List<TriggerCdcStatement> getStatements() {
        return statements;
    }

    public void setStatements(List<TriggerCdcStatement> statements) {
        this.statements = statements;
    }

    public List<TriggerCdcTable> getSupportedTables() {
        return supportedTables;
    }

    public void setSupportedTables(List<TriggerCdcTable> supportedTables) {
        this.supportedTables = supportedTables;
    }

    public List<TriggerCdcTableResult> getBlockedTables() {
        return blockedTables;
    }

    public void setBlockedTables(List<TriggerCdcTableResult> blockedTables) {
        this.blockedTables = blockedTables;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public TriggerCdcInstallPlan blocked(boolean blocked) {
        this.blocked = blocked;
        return this;
    }

    public String getBlockedReason() {
        return blockedReason;
    }

    public void setBlockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
    }

    public TriggerCdcInstallPlan blockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
        return this;
    }
}

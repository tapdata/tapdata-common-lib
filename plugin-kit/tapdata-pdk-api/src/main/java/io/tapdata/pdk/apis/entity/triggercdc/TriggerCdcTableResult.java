package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * Per table outcome of a plan / install / release operation.
 *
 * <p>Installation is reported per object, not as one boolean, because a partial install must be
 * retried idempotently and the operator needs to know which table is missing its trigger.</p>
 *
 * <p>Fail closed: a <b>successful</b> install entry is only a success when it carries the
 * capture identity it created or reused. {@code ensure} assigns
 * {@link #getCaptureId()} per source table and that id is the only thing that can select the
 * journal later, so a success without an id would be a table installed but never read — an
 * invisible data gap. {@link #isSuccess()} therefore returns {@code false} for such an entry and
 * the enclosing {@link TriggerCdcInstallResult} reports it as a failure to fix, instead of
 * quietly dropping it from the read list.</p>
 */
public class TriggerCdcTableResult implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String sourceTableId;
    protected String tableName;
    protected boolean success;
    /** Stable error code the framework can translate; null on success. */
    protected String code;
    /** Human readable reason; for a blocked table this says why it was refused. */
    protected String message;
    protected String captureId;
    protected Long generation;
    protected String changeTableName;
    protected String triggerName;

    public static TriggerCdcTableResult create() {
        return new TriggerCdcTableResult();
    }

    /**
     * Reports a table as installed or reused.
     *
     * <p>Fail closed by construction: the table must carry a non blank
     * {@link TriggerCdcTable#getCaptureId()}, otherwise there is no way to address its journal and
     * the "success" would silently mean "installed but unreadable". Use
     * {@link #failure(TriggerCdcTable, String, String)} for anything that did not become a usable
     * capture.</p>
     *
     * <p>This factory is for {@code ensure} outcomes only. A read only plan reports accepted tables
     * through {@link TriggerCdcInstallPlan#supportedTable(TriggerCdcTable)} and refusals through
     * {@link #failure(TriggerCdcTable, String, String)}; plan time tables have no capture id yet,
     * so calling this there would rightly fail.</p>
     *
     * @throws IllegalArgumentException when {@code table} is null or has no capture id
     */
    public static TriggerCdcTableResult success(TriggerCdcTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null: a success needs the capture it installed");
        }
        if (table.getCaptureId() == null || table.getCaptureId().trim().isEmpty()) {
            throw new IllegalArgumentException("table " + table.getTableName()
                    + " is reported successful but has no captureId; the journal would be unreachable");
        }
        TriggerCdcTableResult result = new TriggerCdcTableResult();
        result.success = true;
        result.sourceTableId = table.getSourceTableId();
        result.tableName = table.getTableName();
        result.captureId = table.getCaptureId();
        result.generation = table.getGeneration();
        result.changeTableName = table.getChangeTableName();
        result.triggerName = table.getTriggerName();
        return result;
    }

    public static TriggerCdcTableResult failure(TriggerCdcTable table, String code, String message) {
        TriggerCdcTableResult result = new TriggerCdcTableResult();
        result.success = false;
        result.code = code;
        result.message = message;
        if (table != null) {
            result.sourceTableId = table.getSourceTableId();
            result.tableName = table.getTableName();
        }
        return result;
    }

    public String getSourceTableId() {
        return sourceTableId;
    }

    public void setSourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
    }

    public TriggerCdcTableResult sourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public TriggerCdcTableResult tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * True only when this entry reports a success <b>and</b> carries a usable capture id.
     *
     * <p>The raw flag is deliberately not enough: {@link #success(boolean)} can be set before
     * {@link #captureId(String)} in a builder chain, and a success that never receives an id must
     * be treated as a failure here rather than passed on as "installed".</p>
     */
    public boolean isSuccess() {
        return success && hasCaptureId();
    }

    /** True when {@link #getCaptureId()} is present and not blank. */
    public boolean hasCaptureId() {
        return captureId != null && !captureId.trim().isEmpty();
    }

    /** The reported success flag, before the capture identity requirement is applied. */
    public boolean isSuccessReported() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the raw success flag. Pair {@code true} with {@link #captureId(String)}: a success
     * without a capture id is reported as a failure by {@link #isSuccess()}.
     */
    public TriggerCdcTableResult success(boolean success) {
        this.success = success;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TriggerCdcTableResult code(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TriggerCdcTableResult message(String message) {
        this.message = message;
        return this;
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public TriggerCdcTableResult captureId(String captureId) {
        this.captureId = captureId;
        return this;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public TriggerCdcTableResult generation(Long generation) {
        this.generation = generation;
        return this;
    }

    public String getChangeTableName() {
        return changeTableName;
    }

    public void setChangeTableName(String changeTableName) {
        this.changeTableName = changeTableName;
    }

    public TriggerCdcTableResult changeTableName(String changeTableName) {
        this.changeTableName = changeTableName;
        return this;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public TriggerCdcTableResult triggerName(String triggerName) {
        this.triggerName = triggerName;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcTableResult{tableName=" + tableName + ", sourceTableId=" + sourceTableId
                + ", success=" + success + ", captureId=" + captureId + ", code=" + code
                + ", message=" + message + "}";
    }
}

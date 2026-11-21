package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * One captured source table.
 *
 * <p>{@code sourceTableId} is the stable identity (a Tapdata table id), while
 * {@code tableName} may be renamed. The journal keys on the id, never on the name, so a
 * rename cannot silently orphan already captured events.</p>
 *
 * <p>{@code captureId} and {@code generation} are assigned by the connector during
 * {@code ensure} and echoed by the framework afterwards. A reinstalled capture gets a new
 * generation so that old offsets are rejected instead of silently resumed.</p>
 *
 * <p>Granularity: one source table is one capture. Each capture owns its own journal, and
 * stream checkpoints and acknowledgements keep independent entries per capture, so a table list is the
 * list of independent read positions a task has to keep.</p>
 */
public class TriggerCdcTable implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String catalog;
    protected String schema;
    /** Physical table name in the source database. */
    protected String tableName;
    /** Stable Tapdata table id. Never derived from the name at read time. */
    protected String sourceTableId;
    /**
     * Assigned by the connector; null in an install request. Ignored on input, echoed on output.
     * It identifies one per table capture, which is also the unit of {@code read} and
     * {@code acknowledge}.
     */
    protected String captureId;
    /** Assigned by the connector; null in an install request. */
    protected Long generation;
    /** Physical journal table that holds the changes of this source table. */
    protected String changeTableName;
    /** Optional trigger name, assigned by the connector. */
    protected String triggerName;

    public static TriggerCdcTable create() {
        return new TriggerCdcTable();
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public TriggerCdcTable catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcTable schema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public TriggerCdcTable tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getSourceTableId() {
        return sourceTableId;
    }

    public void setSourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
    }

    public TriggerCdcTable sourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
        return this;
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public TriggerCdcTable captureId(String captureId) {
        this.captureId = captureId;
        return this;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public TriggerCdcTable generation(Long generation) {
        this.generation = generation;
        return this;
    }

    public String getChangeTableName() {
        return changeTableName;
    }

    public void setChangeTableName(String changeTableName) {
        this.changeTableName = changeTableName;
    }

    public TriggerCdcTable changeTableName(String changeTableName) {
        this.changeTableName = changeTableName;
        return this;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public TriggerCdcTable triggerName(String triggerName) {
        this.triggerName = triggerName;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcTable{catalog=" + catalog + ", schema=" + schema + ", tableName=" + tableName
                + ", sourceTableId=" + sourceTableId + ", captureId=" + captureId + ", generation=" + generation
                + ", changeTableName=" + changeTableName + ", triggerName=" + triggerName + "}";
    }
}

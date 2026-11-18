package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Health report of one capture, produced by
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcInspectFunction}.
 *
 * <p>The report drives the DEGRADED / REPAIR_REQUIRED states. It is also the only honest way to
 * report a structure drift: metadata polling can detect that a table changed, but it cannot
 * reconstruct the DDL history, so the framework must degrade instead of guessing.</p>
 */
public class TriggerCdcInspectResult implements Serializable {
    private static final long serialVersionUID = 1L;

    protected TriggerCdcStatus status;
    protected String captureId;
    protected Long generation;
    protected String sourceTableId;
    protected String sourceTableName;
    protected Long schemaVersion;
    /** State of the trigger, the journal table and the registry entries. */
    protected List<TriggerCdcObjectState> objects;
    /** True when the live table structure no longer matches the registered schema version. */
    protected boolean schemaDrift;
    /** Columns added or dropped since the registered version, when drift was detected. */
    protected List<String> driftedColumns;
    /**
     * True when a change may have been lost, for example a disabled trigger window, a
     * BEST_EFFORT write failure, an overflowed managed DDL buffer, or a forced retention clean
     * that removed records a consumer had not acknowledged (see
     * {@link TriggerCdcCleanupResult#getIntegrityBrokenConsumers()}).
     */
    protected boolean dataIntegrityBroken;
    protected String integrityMessage;
    /** Number of change records not yet acknowledged by any consumer. */
    protected Long unackedCount;
    /** Capture time of the oldest unacknowledged record, for lag monitoring. */
    protected Long oldestUnackedCapturedAt;
    /** Bytes the journal occupies, when the database can report it cheaply. */
    protected Long journalBytes;
    protected List<String> warnings;
    protected List<String> errors;

    public static TriggerCdcInspectResult create() {
        return new TriggerCdcInspectResult();
    }

    public TriggerCdcInspectResult object(TriggerCdcObjectState objectState) {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.add(objectState);
        return this;
    }

    public TriggerCdcInspectResult warning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
        return this;
    }

    public TriggerCdcInspectResult error(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        return this;
    }

    public TriggerCdcStatus getStatus() {
        return status;
    }

    public void setStatus(TriggerCdcStatus status) {
        this.status = status;
    }

    public TriggerCdcInspectResult status(TriggerCdcStatus status) {
        this.status = status;
        return this;
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public String getSourceTableId() {
        return sourceTableId;
    }

    public void setSourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public Long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<TriggerCdcObjectState> getObjects() {
        return objects;
    }

    public void setObjects(List<TriggerCdcObjectState> objects) {
        this.objects = objects;
    }

    public boolean isSchemaDrift() {
        return schemaDrift;
    }

    public void setSchemaDrift(boolean schemaDrift) {
        this.schemaDrift = schemaDrift;
    }

    public List<String> getDriftedColumns() {
        return driftedColumns;
    }

    public void setDriftedColumns(List<String> driftedColumns) {
        this.driftedColumns = driftedColumns;
    }

    public boolean isDataIntegrityBroken() {
        return dataIntegrityBroken;
    }

    public void setDataIntegrityBroken(boolean dataIntegrityBroken) {
        this.dataIntegrityBroken = dataIntegrityBroken;
    }

    public String getIntegrityMessage() {
        return integrityMessage;
    }

    public void setIntegrityMessage(String integrityMessage) {
        this.integrityMessage = integrityMessage;
    }

    public Long getUnackedCount() {
        return unackedCount;
    }

    public void setUnackedCount(Long unackedCount) {
        this.unackedCount = unackedCount;
    }

    public Long getOldestUnackedCapturedAt() {
        return oldestUnackedCapturedAt;
    }

    public void setOldestUnackedCapturedAt(Long oldestUnackedCapturedAt) {
        this.oldestUnackedCapturedAt = oldestUnackedCapturedAt;
    }

    public Long getJournalBytes() {
        return journalBytes;
    }

    public void setJournalBytes(Long journalBytes) {
        this.journalBytes = journalBytes;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

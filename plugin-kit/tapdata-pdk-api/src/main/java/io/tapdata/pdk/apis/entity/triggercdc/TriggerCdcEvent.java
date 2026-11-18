package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.Map;

/**
 * One decoded change record read from the CDC journal.
 *
 * <p>The connector decodes the journal payload (which it wrote itself inside the trigger) into
 * plain Java maps, so the engine can map it onto standard Tap DML / DDL events without knowing
 * the database payload format.</p>
 *
 * <p>Decoding rules that matter for correctness:</p>
 * <ul>
 *   <li>Decimal and big integer values must keep their precision and scale.</li>
 *   <li>Binary, timestamp and time zone values need an explicit, documented encoding.</li>
 *   <li>NULL, missing column and "not captured" must stay distinguishable.</li>
 *   <li>Values are the ones observed at trigger time; never re-read the source row later,
 *       because it may already have been updated again.</li>
 * </ul>
 */
public class TriggerCdcEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Unique id of the change record. Used for acknowledgement and de-duplication.
     *
     * <p>It does <b>not</b> imply commit order, see {@link TriggerCdcOffset}.</p>
     */
    protected String eventId;
    /**
     * Capture this record belongs to. Inside a read batch it always equals the {@code captureId}
     * the caller selected, so an event can never be mistakenly acked against another capture.
     */
    protected String captureId;
    /** Generation of the capture when the record was written. */
    protected Long generation;
    /** Stable id of the source table. */
    protected String sourceTableId;
    protected String sourceTableName;
    protected TriggerCdcOperation operation;
    /** Schema version the payload was encoded with. */
    protected Long schemaVersion;
    /** Primary key before the change, null for INSERT. */
    protected Map<String, Object> keyBefore;
    /** Primary key after the change, null for DELETE. Differs from keyBefore on key updates. */
    protected Map<String, Object> keyAfter;
    /** Full row before the change, when the capture image provides it. */
    protected Map<String, Object> before;
    /** Full row after the change, when the capture image provides it. */
    protected Map<String, Object> after;
    /** Source transaction id, only when {@code transactionIdSupported}. */
    protected String transactionId;
    /** Position inside the source transaction, only when {@code transactionSequenceSupported}. */
    protected Long transactionSeq;
    /** Capture time in epoch millis, for observation only, not a safety cursor. */
    protected Long capturedAt;
    /** Raw or normalized DDL text, only for {@link TriggerCdcOperation#DDL} events. */
    protected String ddlPayload;
    /** Version of the payload encoding. */
    protected Integer payloadVersion;

    public static TriggerCdcEvent create() {
        return new TriggerCdcEvent();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public TriggerCdcEvent eventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public TriggerCdcEvent captureId(String captureId) {
        this.captureId = captureId;
        return this;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public TriggerCdcEvent generation(Long generation) {
        this.generation = generation;
        return this;
    }

    public String getSourceTableId() {
        return sourceTableId;
    }

    public void setSourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
    }

    public TriggerCdcEvent sourceTableId(String sourceTableId) {
        this.sourceTableId = sourceTableId;
        return this;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public TriggerCdcEvent sourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
        return this;
    }

    public TriggerCdcOperation getOperation() {
        return operation;
    }

    public void setOperation(TriggerCdcOperation operation) {
        this.operation = operation;
    }

    public TriggerCdcEvent operation(TriggerCdcOperation operation) {
        this.operation = operation;
        return this;
    }

    public Long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public TriggerCdcEvent schemaVersion(Long schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    public Map<String, Object> getKeyBefore() {
        return keyBefore;
    }

    public void setKeyBefore(Map<String, Object> keyBefore) {
        this.keyBefore = keyBefore;
    }

    public TriggerCdcEvent keyBefore(Map<String, Object> keyBefore) {
        this.keyBefore = keyBefore;
        return this;
    }

    public Map<String, Object> getKeyAfter() {
        return keyAfter;
    }

    public void setKeyAfter(Map<String, Object> keyAfter) {
        this.keyAfter = keyAfter;
    }

    public TriggerCdcEvent keyAfter(Map<String, Object> keyAfter) {
        this.keyAfter = keyAfter;
        return this;
    }

    public Map<String, Object> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    public TriggerCdcEvent before(Map<String, Object> before) {
        this.before = before;
        return this;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }

    public TriggerCdcEvent after(Map<String, Object> after) {
        this.after = after;
        return this;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TriggerCdcEvent transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public Long getTransactionSeq() {
        return transactionSeq;
    }

    public void setTransactionSeq(Long transactionSeq) {
        this.transactionSeq = transactionSeq;
    }

    public TriggerCdcEvent transactionSeq(Long transactionSeq) {
        this.transactionSeq = transactionSeq;
        return this;
    }

    public Long getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Long capturedAt) {
        this.capturedAt = capturedAt;
    }

    public TriggerCdcEvent capturedAt(Long capturedAt) {
        this.capturedAt = capturedAt;
        return this;
    }

    public String getDdlPayload() {
        return ddlPayload;
    }

    public void setDdlPayload(String ddlPayload) {
        this.ddlPayload = ddlPayload;
    }

    public TriggerCdcEvent ddlPayload(String ddlPayload) {
        this.ddlPayload = ddlPayload;
        return this;
    }

    public Integer getPayloadVersion() {
        return payloadVersion;
    }

    public void setPayloadVersion(Integer payloadVersion) {
        this.payloadVersion = payloadVersion;
    }

    public TriggerCdcEvent payloadVersion(Integer payloadVersion) {
        this.payloadVersion = payloadVersion;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcEvent{eventId=" + eventId + ", captureId=" + captureId + ", generation=" + generation
                + ", sourceTableName=" + sourceTableName + ", operation=" + operation
                + ", schemaVersion=" + schemaVersion + ", capturedAt=" + capturedAt + "}";
    }
}

package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * Reader position of one consumer <b>on one capture</b>.
 *
 * <p>Scope: the framework stores one offset per {@code (consumerId, captureId)} pair, never one
 * offset per task. One source table is one capture and owns its own journal, so a task that
 * captures several tables has several independent positions, and each
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcReadFunction read} call is
 * given the offset of the capture it reads. There is no cross capture offset: comparing two
 * captures' tokens or reusing one capture's offset for another is meaningless and must be
 * rejected.</p>
 *
 * <p>Important: {@code lastEventId} is informational only. A database sequence gives unique
 * values, not commit order, so {@code event_id > lastEventId} is <b>not</b> a safe cursor:</p>
 *
 * <pre>
 * tx A allocates event_id=100, not yet committed
 * tx B allocates event_id=101, commits first
 * reader sees 101, advances the cursor to 101
 * tx A commits 100 -&gt; 100 is now permanently skipped
 * </pre>
 *
 * <p>The safe cursor is {@code batchToken}: the connector returns it with a delivered batch and
 * the framework only advances it through
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcAcknowledgeFunction} after
 * the downstream sink is durable. A connector must therefore treat an unacknowledged batch as
 * still pending on every read, even when a larger event id is already visible.</p>
 */
public class TriggerCdcOffset implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Capture this offset belongs to; also the key under which it is persisted. It must equal the
     * {@code captureId} passed to a read call - a mismatch means the checkpoint of another capture
     * was restored and must be rejected instead of resumed.
     */
    protected String captureId;
    /**
     * Generation of the capture. A connector must reject an offset whose generation does not
     * match the installed capture, instead of resuming from a stale position.
     */
    protected Long generation;
    /**
     * Opaque token of the last <b>acknowledged</b> batch. A token that was delivered but not yet
     * acknowledged is never stored here: it lives only in the returned
     * {@link TriggerCdcBatch}, so the connector re-delivers that batch until
     * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcAcknowledgeFunction}
     * reports the downstream write as durable.
     */
    protected String batchToken;
    /** Informational only. Never use it as a safe cursor, see the class javadoc. */
    protected String lastEventId;
    /** Informational only, for lag monitoring. */
    protected Long lastCapturedAt;

    public static TriggerCdcOffset create() {
        return new TriggerCdcOffset();
    }

    /** True when nothing has been acknowledged yet, so the connector starts from the beginning. */
    public boolean isFresh() {
        return batchToken == null || batchToken.isEmpty();
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public TriggerCdcOffset captureId(String captureId) {
        this.captureId = captureId;
        return this;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public TriggerCdcOffset generation(Long generation) {
        this.generation = generation;
        return this;
    }

    public String getBatchToken() {
        return batchToken;
    }

    public void setBatchToken(String batchToken) {
        this.batchToken = batchToken;
    }

    public TriggerCdcOffset batchToken(String batchToken) {
        this.batchToken = batchToken;
        return this;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    public TriggerCdcOffset lastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
        return this;
    }

    public Long getLastCapturedAt() {
        return lastCapturedAt;
    }

    public void setLastCapturedAt(Long lastCapturedAt) {
        this.lastCapturedAt = lastCapturedAt;
    }

    public TriggerCdcOffset lastCapturedAt(Long lastCapturedAt) {
        this.lastCapturedAt = lastCapturedAt;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerCdcOffset{captureId=" + captureId + ", generation=" + generation
                + ", batchToken=" + batchToken + ", lastEventId=" + lastEventId
                + ", lastCapturedAt=" + lastCapturedAt + "}";
    }
}

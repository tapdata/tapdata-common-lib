package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * Delivered position of one consumer on one capture, carried through the existing stream API.
 *
 * <p>StreamReadFunction receives/emits a {@code Map<String, TriggerCdcOffset>} keyed by capture id,
 * even for a one-table task. The map contains the latest delivered token for every subscribed
 * capture. Each callback emits a fresh snapshot (map and values) that must not subsequently be
 * mutated. Events use the existing TapEvent types; no parallel event/batch protocol is needed.</p>
 *
 * <p>A token identifies the cumulative set of events delivered on that capture, including previous
 * unacknowledged batches. It is bound to the consumer, capture and generation. The engine persists
 * the whole map as its normal checkpoint and acknowledges its entries only after all downstream
 * writes and that checkpoint are durable. Delivery alone never permits journal deletion. On
 * restart, unacknowledged records may replay; targets must tolerate at-least-once delivery.</p>
 *
 * <p>Before reading, validate the map against the captures registered by ensure: reject unknown
 * keys, a key/captureId mismatch, missing identities or generations, stale generations, and
 * foreign tokens. A null start offset starts from the registered subscription's beginning;
 * a non-null checkpoint must cover the exact registered capture set, including fresh entries.
 * Do not infer journal routing from a token alone.</p>
 *
 * <p>Sequence allocation is not commit order. For example, transaction A allocates 100, B allocates
 * and commits 101, then A commits 100. Never use {@code event_id > lastEventId} as a permanent
 * cursor. Recheck the committed unacknowledged set, tracking delivered rows during a running
 * session without deleting them. Acknowledge membership in the token's set, never a numeric high
 * watermark. Check capture integrity while polling and fail on drift or disabled triggers.</p>
 */
public class TriggerCdcOffset implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Capture identity; must equal the key under which this offset is checkpointed. */
    protected String captureId;
    /** Installed generation; a mismatch must fail instead of resuming a stale checkpoint. */
    protected Long generation;
    /** Cumulative delivered-set token; only a durable checkpoint authorizes acknowledgement. */
    protected String batchToken;
    /** Informational only. Never use it as a safe cursor, see the class javadoc. */
    protected String lastEventId;
    /** Informational only, for lag monitoring. */
    protected Long lastCapturedAt;

    public static TriggerCdcOffset create() {
        return new TriggerCdcOffset();
    }

    /** True when no batch has been delivered for this position yet. */
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

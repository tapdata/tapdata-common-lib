package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A batch of change records returned by
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcReadFunction}.
 *
 * <p>Scope: one batch holds the records of exactly one capture, the one selected by the
 * {@code captureId} argument of the read call. {@code captureId} and {@code generation} echo that
 * selector and the generation actually read, so a batch can never be acked against another
 * capture. A task that captures several tables receives one batch per capture and does its own
 * merging; this contract does not define a combined batch.</p>
 *
 * <p>The batch carries a {@code batchToken}. The framework must call
 * {@code TriggerCdcAcknowledgeFunction} with that token only after the downstream sink and the
 * recoverable checkpoint are both durable. Returning a batch does <b>not</b> mean the events are
 * consumed.</p>
 *
 * <p>Delivery is at least once: a crash between "sink written" and "batch acknowledged" replays
 * the batch, which is expected and must be tolerated by the target side. End to end exactly once
 * depends on the target write and dedup abilities, never on this contract alone.</p>
 */
public class TriggerCdcBatch implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Opaque token referring to the delivered set of change records of this capture. Connectors
     * should persist the referenced ids (a token can also be a row range marker) instead of
     * forcing the engine to carry a huge id list inside the task checkpoint. A token is scoped to
     * its capture and is only meaningful in {@code acknowledge} for the same {@code captureId}.
     */
    protected String batchToken;
    /**
     * Capture these records belong to; must equal the {@code captureId} the read call selected.
     */
    protected String captureId;
    /**
     * Generation of the capture as read. The framework persists it on the first call, so that a
     * later read with a different generation can be rejected instead of silently resumed.
     */
    protected Long generation;
    protected List<TriggerCdcEvent> events;
    /** True when more unacknowledged records of this capture are waiting right now. */
    protected boolean hasMore;
    /**
     * Total number of records of this capture still unacknowledged for this consumer, when cheap
     * to compute.
     */
    protected Long pendingCount;
    /** Capture time of the oldest unacknowledged record, for lag monitoring. */
    protected Long oldestPendingCapturedAt;

    public static TriggerCdcBatch create() {
        return new TriggerCdcBatch();
    }

    public TriggerCdcBatch event(TriggerCdcEvent event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        return this;
    }

    /** True when this batch carries no change record. */
    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }

    public String getBatchToken() {
        return batchToken;
    }

    public void setBatchToken(String batchToken) {
        this.batchToken = batchToken;
    }

    public TriggerCdcBatch batchToken(String batchToken) {
        this.batchToken = batchToken;
        return this;
    }

    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public TriggerCdcBatch captureId(String captureId) {
        this.captureId = captureId;
        return this;
    }

    public Long getGeneration() {
        return generation;
    }

    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    public TriggerCdcBatch generation(Long generation) {
        this.generation = generation;
        return this;
    }

    public List<TriggerCdcEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TriggerCdcEvent> events) {
        this.events = events;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public TriggerCdcBatch hasMore(boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public TriggerCdcBatch pendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
        return this;
    }

    public Long getOldestPendingCapturedAt() {
        return oldestPendingCapturedAt;
    }

    public void setOldestPendingCapturedAt(Long oldestPendingCapturedAt) {
        this.oldestPendingCapturedAt = oldestPendingCapturedAt;
    }

    public TriggerCdcBatch oldestPendingCapturedAt(Long oldestPendingCapturedAt) {
        this.oldestPendingCapturedAt = oldestPendingCapturedAt;
        return this;
    }
}

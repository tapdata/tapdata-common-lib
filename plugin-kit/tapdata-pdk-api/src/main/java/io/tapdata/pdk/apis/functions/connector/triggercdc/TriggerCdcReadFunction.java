package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcBatch;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcOffset;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

/**
 * Reads committed, not yet acknowledged change records for one consumer on one capture.
 *
 * <p>Scope of a single call: exactly one capture. {@link TriggerCdcEnsureFunction} installs one
 * capture per source table and assigns its {@link TriggerCdcTable#getCaptureId()}, and every
 * capture owns its own journal table. A task that captures several tables therefore owns several
 * captures and must call {@code read} once per capture, each with its own offset. One call never
 * spans two captures: a journal table, a batch token and an offset all belong to exactly one
 * capture, and mixing them would make "acknowledged" ambiguous. Merging and ordering the batches
 * of different captures is the framework's job, not this function's.</p>
 *
 * <p>This is deliberately not a {@code streamRead}: reliable consumption must not depend on the
 * progress of a callback. The read returns a batch plus a token, and only
 * {@link TriggerCdcAcknowledgeFunction} moves the position.</p>
 *
 * <p>Paging rules that prevent silent loss:</p>
 * <ul>
 *   <li>Never treat {@code event_id > last_event_id} as the cursor. A sequence gives unique
 *       values, not commit order, so a late committing transaction with a smaller id would be
 *       skipped forever. Each call must re-check the whole unacknowledged set of this capture;
 *       ids may be used for paging inside a round, not to permanently exclude smaller ids.</li>
 *   <li>Only committed rows may be returned. Records of a rolled back transaction must never
 *       become visible.</li>
 *   <li>A {@code captureId} that is null or unknown must be rejected, not guessed: a fresh offset
 *       carries no identity, so the selector is the only thing that can pick the journal.</li>
 *   <li>A generation that differs from the installed capture must be rejected, never reused. The
 *       returned batch always echoes the generation actually read, so the framework can persist
 *       it after the first call.</li>
 *   <li>When the batch is empty, return an empty batch rather than null.</li>
 * </ul>
 */
public interface TriggerCdcReadFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration
     * @param consumerId       stable id of the subscribing task / channel
     * @param captureId        capture to read, taken from the {@code captureId} of a successful
     *                         {@link TriggerCdcEnsureFunction} result (see
     *                         {@code TriggerCdcInstallResult#getCaptureIds()}). It is the routing
     *                         authority: only the journal of this capture may be read, and the
     *                         returned batch must echo it. One capture per call; a multi table
     *                         task iterates its captures and merges the batches itself.
     * @param offset           last acknowledged position <b>of this capture for this consumer</b>;
     *                         the framework keeps one offset per {@code (consumerId, captureId)}.
     *                         Its {@link TriggerCdcOffset#getBatchToken()} is the last
     *                         <b>acknowledged</b> batch token, never a merely delivered one.
     *                         {@code null} or a fresh offset means start from the beginning of this
     *                         capture's journal. A non-null
     *                         {@link TriggerCdcOffset#getCaptureId()} must equal {@code captureId};
     *                         a mismatch must be rejected, because it means a checkpoint of
     *                         another capture was restored here.
     * @param batchSize        maximum number of records to return in this call; it wins over
     *                         {@link TriggerCdcConfig#getBatchSize()}, which the framework only uses
     *                         as the default it passes in here
     * @return the batch of this capture, never null
     */
    TriggerCdcBatch read(TapConnectorContext connectorContext, TriggerCdcConfig config, String consumerId,
                         String captureId, TriggerCdcOffset offset, int batchSize) throws Throwable;
}

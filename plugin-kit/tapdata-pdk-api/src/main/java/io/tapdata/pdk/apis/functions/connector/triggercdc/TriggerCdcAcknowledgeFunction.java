package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

/**
 * Marks a delivered batch of <b>one capture</b> as safely processed by one consumer.
 *
 * <p>The framework calls this only after the downstream write and the recoverable checkpoint are
 * both durable. Connectors must treat the acknowledgement as the single source of truth for the
 * consumer position on that capture, and must keep the referenced records visible until every
 * live consumer has acknowledged them.</p>
 *
 * <p>Acknowledgement is per capture, mirroring {@link TriggerCdcReadFunction}: a task that
 * captures several tables acknowledges each capture separately, so one capture can never appear
 * consumed because a sibling capture advanced. A token belongs to exactly one capture and may
 * only be acknowledged against it.</p>
 *
 * <p>Acknowledgement is the reason the mode can be called reliable: a crash between "written" and
 * "acknowledged" replays the batch (at least once), and the target side must be idempotent.</p>
 */
public interface TriggerCdcAcknowledgeFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration
     * @param consumerId       stable id of the subscribing task / channel
     * @param captureId        capture the token belongs to, the same value that was passed to
     *                         {@link TriggerCdcReadFunction}. A token that does not belong to this
     *                         capture must be rejected instead of acknowledged.
     * @param batchToken       token returned by {@link TriggerCdcReadFunction} for the batch of
     *                         this capture
     */
    void acknowledge(TapConnectorContext connectorContext, TriggerCdcConfig config, String consumerId,
                     String captureId, String batchToken) throws Throwable;
}

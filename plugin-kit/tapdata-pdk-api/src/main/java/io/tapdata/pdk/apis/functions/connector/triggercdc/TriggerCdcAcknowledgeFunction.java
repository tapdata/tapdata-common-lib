package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcOffset;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

/**
 * Confirms a recoverable checkpoint of one consumer on one capture. Idempotent.
 *
 * <p>The engine calls this only after every downstream destination has durably written the
 * covered events and the corresponding checkpoint is durably recoverable. Returning from
 * StreamReadConsumer.accept means delivery to the engine, not durable acknowledgement. Until
 * that durable engine integration exists, cleanup must not treat delivered events as consumed.</p>
 *
 * <p>Reject null/blank consumer ids, missing capture id/generation/token, unknown subscriptions,
 * and tokens not issued to that consumer, capture and generation. A token covers the cumulative
 * delivered set of its capture, including earlier unacknowledged batches. Acknowledge exactly
 * that set, never every row with a smaller event id: allocation order is not commit order. A
 * delayed/repeated acknowledgement must not regress progress. Persist acknowledgement before
 * returning; a crash between checkpoint persistence and this call safely causes replay.</p>
 */
public interface TriggerCdcAcknowledgeFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the node context
     * @param config effective connection-level trigger CDC configuration
     * @param consumerId stable subscription identity passed to ensure
     * @param offset one capture's offset from a durable checkpoint, never a delivery-only offset
     */
    void acknowledge(TapConnectorContext connectorContext, TriggerCdcConfig config, String consumerId,
                     TriggerCdcOffset offset) throws Throwable;
}

package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

/**
 * Releases a consumer, and optionally the capture objects themselves.
 *
 * <p>Two separate actions on purpose:</p>
 * <ul>
 *   <li>Unregistering a consumer stops its claim on journal records, so cleanup may remove them.
 *       A consumer holds one position per capture, so unregistering must drop <b>all</b> of them
 *       at once; a leftover position would keep a journal alive forever. This must be an explicit
 *       operation, never an implicit side effect of a crash or a timeout, otherwise a temporarily
 *       paused task silently loses its data.</li>
 *   <li>Dropping objects happens only when the framework owns them and no other consumer
 *       references the capture. Never drop an entire customer schema.</li>
 * </ul>
 */
public interface TriggerCdcReleaseFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration
     * @param consumerId       consumer to unregister; null skips the unregistration step
     * @param tables           captures to consider; ignored when {@code dropObjects} is false
     * @param dropObjects      true to also remove the triggers and journal objects Tapdata owns
     */
    void release(TapConnectorContext connectorContext, TriggerCdcConfig config, String consumerId,
                 List<TriggerCdcTable> tables, boolean dropObjects) throws Throwable;
}

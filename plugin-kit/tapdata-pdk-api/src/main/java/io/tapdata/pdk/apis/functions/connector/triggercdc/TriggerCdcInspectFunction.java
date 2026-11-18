package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcInspectResult;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

/**
 * Health and integrity inspection of installed captures.
 *
 * <p>Used for periodic monitoring and before resuming a task. It must detect at least: a missing
 * or disabled trigger, a missing journal table, a structure drift against the registered schema
 * version, backlog size and lag, and journal growth.</p>
 *
 * <p>Drift detection is not DDL capture. Once drift is detected the capture must be marked
 * degraded, because the DDL history between two inspections cannot be reconstructed and the
 * changes of the missed window cannot be replayed.</p>
 */
public interface TriggerCdcInspectFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration
     * @param tables           captures to inspect; empty means all captures known to the registry
     * @return one report per capture, never null
     */
    List<TriggerCdcInspectResult> inspect(TapConnectorContext connectorContext, TriggerCdcConfig config, List<TriggerCdcTable> tables) throws Throwable;
}

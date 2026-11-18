package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcCapabilities;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

/**
 * Declares what trigger based CDC this connector really supports.
 *
 * <p>Registering this function is what enables the {@code triggerCdc} increment mode for a
 * connector. A connector without it must not be offered the mode, and the framework must not
 * call any other Trigger CDC function on it.</p>
 *
 * <p>Declarations are a contract, not a hint: the framework relies on them to decide whether a
 * capture boundary exists, whether DDL may be replayed, and whether a full-then-incremental
 * handover is safe. Overstating a capability causes silent data loss.</p>
 */
public interface TriggerCdcCapabilitiesFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @return the declaration for the connected database and its version; never null
     * @throws Throwable when the database version cannot be determined, so the mode stays
     *                   unavailable instead of being guessed
     */
    TriggerCdcCapabilities capabilities(TapConnectorContext connectorContext) throws Throwable;
}

package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcInstallPlan;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

/**
 * Builds the installation plan without touching the database.
 *
 * <p>Must be side effect free so the UI can show objects, statements, required privileges,
 * warnings and blocking problems before anything is created. The same statements feed the
 * {@code MANUAL} install path used by a DBA, so they must match what {@code ensure} would run.</p>
 *
 * <p>A plan must report problems instead of skipping them: a table whose types cannot be
 * captured, or a table without a primary key, belongs in
 * {@link TriggerCdcInstallPlan#getBlockedTables()} so the operator sees the gap.</p>
 */
public interface TriggerCdcPlanFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration; already defaulted
     * @param tables           source tables the task wants to capture
     * @return the plan, never null
     */
    TriggerCdcInstallPlan plan(TapConnectorContext connectorContext, TriggerCdcConfig config, List<TriggerCdcTable> tables) throws Throwable;
}

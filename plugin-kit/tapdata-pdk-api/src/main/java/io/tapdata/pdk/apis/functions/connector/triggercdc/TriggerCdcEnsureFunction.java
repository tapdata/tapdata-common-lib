package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcInstallResult;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

/**
 * Installs and verifies capture objects and registers a durable consumer before snapshot/read.
 *
 * <p>Idempotent for the same consumer and tables: reuse capture ids and generations. Increment
 * the generation only on a real reinstall and reject old checkpoints afterwards. Never overwrite
 * objects Tapdata does not own. Bound installation lock waits and report each table's outcome,
 * including failures. Each success needs a unique nonblank capture id and a generation.</p>
 *
 * <p>Database/version-specific capabilities are returned in the install result rather than
 * obtained through a separate function. Validate requested options and unsupported types before
 * changing objects. A capture boundary must be verified, not inferred from CREATE TRIGGER
 * returning. A verified installation boundary alone does not prove safe snapshot/journal
 * handover: overlap handling and durable snapshot/replay recovery must also be implemented and
 * verified before automatic full-to-incremental handover is enabled. See docs/trigger-cdc.md.</p>
 *
 * <p>Call again before resume to verify triggers, journal and schema versions. The registered
 * {@link io.tapdata.pdk.apis.functions.connector.source.StreamReadFunction} polls those captures,
 * emits ordinary TapEvents, and must stop on missing/disabled triggers, schema drift or a broken
 * consumer. There is no separate read, plan, inspect or release function in this version.
 * Subscriptions survive stop, crash and task deletion until explicitly retired outside this API;
 * they must never expire based on time alone.</p>
 *
 * <p>The connector retains this consumer/config/capture mapping in its node-scoped runtime for
 * streamRead; do not put task-specific state in a shared connection. The engine must validate the
 * result against its original table list and persist the capture identities before starting reads.
 * A partial installation must not silently shrink the requested task.</p>
 */
public interface TriggerCdcEnsureFunction extends TapConnectorFunction {
    /**
     * @param connectorContext node-scoped connection context used for the subsequent streamRead
     * @param config effective connection-level storage and capture configuration
     * @param consumerId stable, nonblank subscription identity, preserved across task restarts
     * @param tables source tables; exactly one outcome per requested table, retained in the result
     * @return installation and verified capabilities; never null
     */
    TriggerCdcInstallResult ensure(TapConnectorContext connectorContext, TriggerCdcConfig config,
                                  String consumerId, List<TriggerCdcTable> tables) throws Throwable;
}

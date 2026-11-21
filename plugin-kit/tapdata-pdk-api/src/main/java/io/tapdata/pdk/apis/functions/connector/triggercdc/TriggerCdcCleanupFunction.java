package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcCleanupResult;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

/**
 * Reclaims journal space for records that every live consumer has acknowledged.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Delete acknowledged records only. Retention does not override acknowledgement: an
 *       unacknowledged record is reported through
 *       {@link TriggerCdcCleanupResult#getRemainingCount()} and kept, so the framework can
 *       alarm. The only exception is the explicit opt-in
 *       {@link TriggerCdcConfig#getForceCleanUnacknowledged()}, which defaults to {@code false}.</li>
 *   <li>Always fill in {@link TriggerCdcCleanupResult#getDeletedUnacknowledgedCount()} — {@code 0}
 *       when only acknowledged records were removed. This is what makes the acknowledgement rule
 *       enforceable: the framework calls {@link TriggerCdcCleanupResult#validate(TriggerCdcConfig)}
 *       on the returned object and rejects a report that deletes unacknowledged records without
 *       the opt-in or without naming the affected consumers.</li>
 *   <li>When the opt-in is enabled and unacknowledged records are reclaimed, the connector must
 *       report every affected consumer through
 *       {@link TriggerCdcCleanupResult#getIntegrityBrokenConsumers()} and persist that marking
 *       no later than the delete itself — in the same transaction when the engine supports it,
 *       otherwise by marking first and deleting second, so no window exists in which the rows are
 *       gone and the consumers look healthy. A forced clean that reports no affected consumer is a
 *       contract violation, because the framework would silently keep running a consumer with a
 *       hole in its stream.</li>
 *   <li>Keep the journal bounded per batch; do not run one unbounded DELETE that holds locks on
 *       the customer database.</li>
 *   <li>Records referenced by a registered consumer that is merely paused must survive until that
 *       consumer is explicitly unregistered — unless the force clean opt-in above applies, in
 *       which case the consumer has to be reported as integrity broken.</li>
 * </ul>
 */
public interface TriggerCdcCleanupFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration: retention window plus the
     *                         force clean opt-in that decides whether unacknowledged records may
     *                         ever be reclaimed
     * @return what was removed and what was kept, never null; when unacknowledged records were
     * removed, {@link TriggerCdcCleanupResult#getDeletedUnacknowledgedCount()} is greater than
     * {@code 0} and the affected consumers are non empty
     */
    TriggerCdcCleanupResult cleanup(TapConnectorContext connectorContext, TriggerCdcConfig config) throws Throwable;
}

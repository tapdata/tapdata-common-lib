package io.tapdata.pdk.apis.functions.connector.triggercdc;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcConfig;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcInstallResult;
import io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcTable;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

/**
 * Creates the CDC objects and triggers, then verifies them. Idempotent.
 *
 * <p>Requirements:</p>
 * <ul>
 *   <li>Running it twice returns the same capture ids and the same generation. Only a real
 *       reinstall increments the generation, so a stale offset can be rejected instead of
 *       silently resumed.</li>
 *   <li>Never overwrite an object Tapdata does not own. Report a conflict instead.</li>
 *   <li>Creating or replacing a trigger may lock the source table: use a short lock timeout,
 *       fail fast and stay retryable instead of waiting on customer traffic.</li>
 *   <li>Success must include the verification that the trigger is really enabled and that the
 *       installed capture set covers everything the caller asked for.</li>
 *   <li>Each successful table result must carry a non blank {@code captureId} that is unique
 *       within the result: that id is the only address of the journal. A success without an id, or
 *       two tables sharing an id, is rejected as an invalid result (see
 *       {@code TriggerCdcInstallResult#validate()}) instead of shrinking the capture list —
 *       dropping either would mean an installed table that is never read.</li>
 *   <li>Every requested table needs its own outcome, success <b>or</b> failure: a table that is
 *       simply absent from the result is neither readable nor retryable, so it is the worst
 *       outcome of all. Report it through {@code TriggerCdcInstallResult#requestedTables(...)}
 *       (or let the framework pass {@code tables} to {@code validate(tables)}) so the check can
 *       tell an omitted table from an unrequested one. Three shapes are rejected: a requested
 *       table with no outcome, one table with several outcomes, and an outcome for a table that
 *       was never requested.</li>
 * </ul>
 *
 * <p>Whether installation forms a valid capture boundary is declared through
 * {@code TriggerCdcCapabilities.captureBoundarySupported}. "CREATE TRIGGER returned" alone is
 * never proof that no concurrent transaction was missed.</p>
 */
public interface TriggerCdcEnsureFunction extends TapConnectorFunction {
    /**
     * @param connectorContext the connection / node context
     * @param config           connection level trigger CDC configuration
     * @param tables           source tables to capture; capture ids and generations are filled in
     *                         by the connector. One table yields one capture, and that capture is
     *                         the unit of {@link TriggerCdcReadFunction} and
     *                         {@link TriggerCdcAcknowledgeFunction}
     * @return per table outcome, never null, with exactly one outcome per requested table (a
     *         failure is a valid outcome; silence is not) and the request retained through
     *         {@code TriggerCdcInstallResult#requestedTables(Collection)}. The framework passes
     *         {@code tables} to {@code validate(tables)} / {@code getCaptureIds(tables)}, so a
     *         result that does not account for every requested table is rejected rather than read
     *         as a smaller capture set
     */
    TriggerCdcInstallResult ensure(TapConnectorContext connectorContext, TriggerCdcConfig config, List<TriggerCdcTable> tables) throws Throwable;
}

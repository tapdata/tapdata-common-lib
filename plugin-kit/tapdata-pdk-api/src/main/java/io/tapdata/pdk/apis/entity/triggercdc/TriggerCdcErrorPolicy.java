package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * What happens when the trigger fails to append the change record into the CDC journal.
 *
 * <p>"Never block the business" and "never lose a change" cannot both be guaranteed by a
 * plain trigger, because the trigger body normally runs inside the business transaction.
 * This enum makes the trade off explicit instead of hiding it.</p>
 */
public enum TriggerCdcErrorPolicy {
    /**
     * Default, and the only policy a connector must implement. If the CDC journal write
     * fails, the business statement fails as well, so the capture stays complete.
     */
    STRICT,
    /**
     * The business statement is allowed to continue, so changes may be lost. A task using this
     * policy must be marked {@link TriggerCdcStatus#REPAIR_REQUIRED} — not merely
     * {@link TriggerCdcStatus#DEGRADED}, because a loss is not a state that operator attention
     * alone can fix — and requires a re-check or a full resync. Only exposed when the connector
     * declares it in {@link TriggerCdcCapabilities#getErrorPolicies()}.
     */
    BEST_EFFORT
}

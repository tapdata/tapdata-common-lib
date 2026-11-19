package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of
 * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcCleanupFunction}.
 *
 * <p>One rule decides what this object may report: a change record may be removed only after
 * every live consumer acknowledged it. Retention is not an exception to that rule by default —
 * an unacknowledged record is reported through {@link #getRemainingCount()} instead of being
 * deleted.</p>
 *
 * <p>The single exception is opted into explicitly through
 * {@link TriggerCdcConfig#getForceCleanUnacknowledged()} (default {@code false}). When it is
 * enabled and the connector reclaims records that were not acknowledged, it must (a) count them
 * in {@link #getDeletedUnacknowledgedCount()}, (b) list every affected consumer in
 * {@link #getIntegrityBrokenConsumers()}, and (c) persist that marking together with the delete.
 * Reclaiming unacknowledged records without reporting the affected consumers is silent data loss
 * and a contract violation, not a tuning choice.</p>
 *
 * <p>Because (a) and (b) are plain data, the framework can check the contract instead of trusting
 * prose: call {@link #validate(TriggerCdcConfig)} on every returned result and reject a cleanup
 * that reports inconsistencies. A connector that removes rows and leaves
 * {@link #getDeletedUnacknowledgedCount()} at {@code 0} is claiming it removed nothing
 * unacknowledged, and is answerable for that claim.</p>
 */
public class TriggerCdcCleanupResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Records removed by this call. */
    protected long deletedCount;
    /**
     * Records kept because at least one live consumer has not acknowledged them. A non zero value
     * is not an error: it is the signal the framework alarms on and the input for capacity
     * planning.
     */
    protected long remainingCount;
    /**
     * How many of {@link #getDeletedCount()} were not acknowledged by every live consumer — the
     * only records that can damage a consumer. Must be {@code 0} outside the force clean opt-in.
     *
     * <p>This field exists so the contract is checkable rather than aspirational: without it a
     * connector could delete unacknowledged rows, report a total {@code deletedCount}, and leave
     * {@link #getIntegrityBrokenConsumers()} empty with nothing able to tell.</p>
     */
    protected long deletedUnacknowledgedCount;
    /**
     * Consumers whose data is incomplete because this call removed records they had not
     * acknowledged. Must be empty when {@link #getDeletedUnacknowledgedCount()} is {@code 0}, and
     * must list every affected consumer when it is greater than {@code 0}: a forced clean that
     * deletes unacknowledged rows but reports no consumer is rejected by the framework.
     */
    protected List<String> integrityBrokenConsumers;
    protected List<String> warnings;

    public static TriggerCdcCleanupResult create() {
        return new TriggerCdcCleanupResult();
    }

    public TriggerCdcCleanupResult warning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
        return this;
    }

    /**
     * Reports a consumer whose stream now has a hole. Only valid together with a non zero
     * {@link #getDeletedUnacknowledgedCount()}: a hole can only come from a removed record.
     */
    public TriggerCdcCleanupResult integrityBrokenConsumer(String consumerId) {
        if (integrityBrokenConsumers == null) {
            integrityBrokenConsumers = new ArrayList<>();
        }
        integrityBrokenConsumers.add(consumerId);
        return this;
    }

    /** True when this call removed records that a consumer had not acknowledged. */
    public boolean hasIntegrityBrokenConsumers() {
        return integrityBrokenConsumers != null && !integrityBrokenConsumers.isEmpty();
    }

    /**
     * Checks this result against the rules in this class and returns one message per violation;
     * an empty list means the result is a valid cleanup outcome.
     *
     * <p>The framework must call this after every {@code cleanup} invocation and treat a non empty
     * list the same as {@link TriggerCdcErrorPolicy#STRICT} would treat a failed write: the delete
     * is not trusted and the affected tasks are marked integrity broken. The check cannot verify
     * what the database actually did, only that the report is internally consistent — a connector
     * that both deletes unacknowledged records and hides them is out of contract, and no result
     * object can catch that.</p>
     *
     * @param config the configuration the cleanup ran with, or {@code null} when unknown; with a
     *               {@code null} config only the report's internal consistency is checked
     */
    public List<String> validate(TriggerCdcConfig config) {
        List<String> violations = new ArrayList<>();
        if (deletedCount < 0) {
            violations.add("deletedCount must not be negative: " + deletedCount);
        }
        if (remainingCount < 0) {
            violations.add("remainingCount must not be negative: " + remainingCount);
        }
        if (deletedUnacknowledgedCount < 0) {
            violations.add("deletedUnacknowledgedCount must not be negative: " + deletedUnacknowledgedCount);
        }
        if (deletedUnacknowledgedCount > deletedCount) {
            violations.add("deletedUnacknowledgedCount (" + deletedUnacknowledgedCount
                    + ") exceeds deletedCount (" + deletedCount + ")");
        }
        if (deletedUnacknowledgedCount > 0) {
            if (config != null && !config.forceCleanUnacknowledgedEnabled()) {
                violations.add("removed " + deletedUnacknowledgedCount
                        + " unacknowledged record(s) while forceCleanUnacknowledged is false");
            }
            if (hasIntegrityBrokenConsumers()) {
                for (String consumerId : integrityBrokenConsumers) {
                    if (consumerId == null || consumerId.trim().isEmpty()) {
                        violations.add("integrity broken consumer id must not be null or blank");
                    }
                }
            }
            if (!hasIntegrityBrokenConsumers()) {
                violations.add("removed " + deletedUnacknowledgedCount
                        + " unacknowledged record(s) but reported no integrity broken consumer");
            }
        } else if (hasIntegrityBrokenConsumers()) {
            violations.add("reported integrity broken consumer(s) " + integrityBrokenConsumers
                    + " but deletedUnacknowledgedCount is 0");
        }
        return violations;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public TriggerCdcCleanupResult deletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
        return this;
    }

    public long getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(long remainingCount) {
        this.remainingCount = remainingCount;
    }

    public TriggerCdcCleanupResult remainingCount(long remainingCount) {
        this.remainingCount = remainingCount;
        return this;
    }

    public long getDeletedUnacknowledgedCount() {
        return deletedUnacknowledgedCount;
    }

    public void setDeletedUnacknowledgedCount(long deletedUnacknowledgedCount) {
        this.deletedUnacknowledgedCount = deletedUnacknowledgedCount;
    }

    public TriggerCdcCleanupResult deletedUnacknowledgedCount(long deletedUnacknowledgedCount) {
        this.deletedUnacknowledgedCount = deletedUnacknowledgedCount;
        return this;
    }

    public List<String> getIntegrityBrokenConsumers() {
        return integrityBrokenConsumers;
    }

    public void setIntegrityBrokenConsumers(List<String> integrityBrokenConsumers) {
        this.integrityBrokenConsumers = integrityBrokenConsumers;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

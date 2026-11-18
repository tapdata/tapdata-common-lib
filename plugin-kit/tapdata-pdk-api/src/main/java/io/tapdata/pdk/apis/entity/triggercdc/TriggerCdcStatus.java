package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * Lifecycle state of one capture (one source table, one generation).
 *
 * <pre>
 * PLANNED -&gt; INSTALLING -&gt; CAPTURING -&gt; SNAPSHOTTING -&gt; STREAMING
 *                                |                              |
 *                                +---------&gt; DEGRADED / REPAIR_REQUIRED
 * </pre>
 */
public enum TriggerCdcStatus {
    /** Plan generated, nothing created yet. */
    PLANNED,
    /** Objects / triggers are being created. Failure is retryable and reported per object. */
    INSTALLING,
    /** Triggers are live and journaling changes, no consumer is reading yet. */
    CAPTURING,
    /** A full snapshot is running while the journal keeps accumulating changes. */
    SNAPSHOTTING,
    /** Incremental consumption is running normally. */
    STREAMING,
    /**
     * Capture still works and no change is known to be lost, but a declared guarantee is not
     * met: for example a structure drift that still needs an operator decision, journal growth
     * beyond the expected volume, or an unregistered consumer holding retention. Requires
     * operator attention.
     */
    DEGRADED,
    /**
     * A change is known or suspected to be lost, so the target can no longer be trusted: a
     * {@link TriggerCdcErrorPolicy#BEST_EFFORT} journal write failure, a window in which the
     * trigger was disabled, an overflowed managed DDL buffer, or a forced clean that removed
     * records a consumer had not acknowledged (see
     * {@link TriggerCdcCleanupResult#getIntegrityBrokenConsumers()}). A repair or a full resync
     * is required.
     */
    REPAIR_REQUIRED,
    /** Installation or a later verification failed. */
    FAILED,
    /** Objects released, no longer capturing. */
    RELEASED
}

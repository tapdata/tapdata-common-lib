package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * How structural changes of the captured source tables are handled.
 *
 * <p>"This database supports triggers" does not imply "this connector supports DDL capture".
 * Each connector declares which levels it can honour in
 * {@link TriggerCdcCapabilities#getDdlModes()}.</p>
 */
public enum TriggerCdcDdlMode {
    /**
     * DDL is captured by a native database facility (for example a PostgreSQL event trigger).
     * The adapter must declare exactly which DDL statements are observable.
     */
    NATIVE_DDL,
    /**
     * DDL goes through a coordinated workflow: register a new schema version, rebuild the
     * trigger, then switch consumption from the old to the new schema version behind a table
     * level barrier.
     *
     * <p>DML arriving inside the coordination window is buffered; exceeding the buffer means
     * changes may have been dropped, so the capture must be marked
     * {@link TriggerCdcStatus#REPAIR_REQUIRED} instead of pretending the switch was lossless.</p>
     */
    MANAGED_DDL,
    /**
     * Default for the first release. The framework periodically compares the live table
     * structure and the installed trigger set against the registered schema version, and
     * alarms on drift.
     *
     * <p>Detection cannot reconstruct the DDL history between two checks, so it never claims
     * lossless DDL replication.</p>
     */
    DETECT_ONLY
}

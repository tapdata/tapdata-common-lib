package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * The kind of change a trigger based CDC journal entry represents.
 *
 * <p>A connector must only report the operations that it declared in
 * {@link TriggerCdcCapabilities#getDmlOperations()}. Reporting an operation the database
 * trigger cannot really observe would silently lose data.</p>
 */
public enum TriggerCdcOperation {
    INSERT,
    UPDATE,
    DELETE,
    /**
     * Structural change captured by a native DDL trigger / event trigger, or by the managed
     * DDL workflow. See {@link TriggerCdcDdlMode}.
     */
    DDL,
    /**
     * Table level {@code TRUNCATE}, observed as its own event.
     *
     * <p>This is not a row level operation and must never be expanded into a stream of DELETE
     * events, because the number of affected rows is unknown at capture time. An event of this kind
     * carries the table identity only: {@code before} / {@code after} / key images are null, and
     * the sink has to apply a truncate (or a documented equivalent such as a partition drop).</p>
     *
     * <p>Duplicated authority is forbidden: a connector reports this operation if and only if
     * {@link TriggerCdcCapabilities#isTruncateCaptureSupported()} is {@code true}. Reporting one
     * without the other is out of contract.</p>
     */
    TRUNCATE
}

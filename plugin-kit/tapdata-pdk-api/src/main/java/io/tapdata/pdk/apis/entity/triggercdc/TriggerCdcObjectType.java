package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * The kinds of database objects a capture needs.
 *
 * <p>Object names are suggestions owned by the framework
 * (see {@link TriggerCdcConstants}); the connector maps them onto real database
 * semantics (catalog / schema / database / keyspace...).</p>
 */
public enum TriggerCdcObjectType {
    /** The namespace (schema / database) that holds the CDC objects. */
    STORAGE_NAMESPACE,
    /** Registry of installed captures: source table identity, trigger names, generation, state. */
    CAPTURE_REGISTRY,
    /** Immutable history of registered table structure versions. */
    SCHEMA_HISTORY,
    /** Per source table journal of captured changes. */
    CHANGE_TABLE,
    /** Per consumer persistence of acknowledged events. */
    CONSUMER_ACK,
    /** Optional bucket for capture errors the database can isolate safely. */
    CAPTURE_ERROR,
    /** The trigger (or trigger set) on the source table itself. */
    TRIGGER
}

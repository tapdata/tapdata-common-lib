package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * Names and defaults shared by the framework and every Trigger CDC connector.
 *
 * <p>These values are the protocol, not a database specific detail: the framework uses them to
 * talk about the CDC storage layout, the connector uses them when it materialises the objects.
 * A connector may only deviate from the names when the database cannot represent them, and it
 * must then report the real names back through {@link TriggerCdcTable#getChangeTableName()}.</p>
 */
public final class TriggerCdcConstants {
    private TriggerCdcConstants() {
    }

    /**
     * Capability id the framework exposes when a connector registered the Trigger CDC
     * functions. Manager / UI can use it as a single flag to show the mode at all.
     */
    public static final String CAPABILITY_TRIGGER_CDC = "trigger_cdc";

    /**
     * Capability ids are derived from the {@link io.tapdata.pdk.apis.functions.ConnectorFunctions}
     * field names. They are listed here so callers do not hard code strings.
     */
    public static final String CAPABILITY_TRIGGER_CDC_ENSURE = "trigger_cdc_ensure_function";
    public static final String CAPABILITY_TRIGGER_CDC_ACKNOWLEDGE = "trigger_cdc_acknowledge_function";
    public static final String CAPABILITY_TRIGGER_CDC_CLEANUP = "trigger_cdc_cleanup_function";

    /** Default CDC storage namespace when the user did not pick one. */
    public static final String DEFAULT_STORAGE_SCHEMA = "cdc";

    /** Registry of installed captures. */
    public static final String OBJECT_CAPTURE_REGISTRY = "tap_capture_registry";
    /** Immutable table structure versions. */
    public static final String OBJECT_SCHEMA_HISTORY = "tap_schema_history";
    /** Per source table journal, suffix is appended to this prefix. */
    public static final String OBJECT_CHANGE_TABLE_PREFIX = "tap_changes_";
    /** Per consumer acknowledgement of delivered events. */
    public static final String OBJECT_CONSUMER_ACK = "tap_consumer_ack";
    /** Optional capture error bucket. */
    public static final String OBJECT_CAPTURE_ERROR = "tap_capture_errors";

    /** Default polling interval of the journal reader. To be tuned by load testing. */
    public static final long DEFAULT_POLL_INTERVAL_MS = 500L;
    /** Default batch size of the journal reader. To be tuned by load testing. */
    public static final int DEFAULT_BATCH_SIZE = 1000;
    /**
     * Default journal retention, matching a 7 day operational window.
     *
     * <p>Retention applies to acknowledged records only. It is not a licence to delete a record a
     * live consumer still needs: that requires the explicit
     * {@link TriggerCdcConfig#getForceCleanUnacknowledged()} opt-in.</p>
     */
    public static final long DEFAULT_RETENTION_SECONDS = 7L * 24L * 60L * 60L;

    /**
     * Version of the journal payload encoding. Written next to every change record so that a
     * later reader can decode records written by an older connector build.
     */
    public static final int PAYLOAD_VERSION = 1;

    /** Database name prefix of the generated trigger, before the source table name. */
    public static final String TRIGGER_NAME_PREFIX = "tap_trg_";
}

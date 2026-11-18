package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;

/**
 * Connection level Trigger CDC configuration: where the journal lives and how it behaves.
 *
 * <p>Ownership rule: the storage location ({@code catalog} / {@code schema}) is defined once
 * per connection, while table subscriptions and reader tuning belong to the task. Two tasks
 * capturing the same source table must share one set of capture objects, so the storage
 * location must not be derived from the task.</p>
 */
public class TriggerCdcConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Catalog of the CDC storage. Null when the database has no catalog concept; the
     * connector then ignores it. Never guess a default, because writing the journal into the
     * wrong catalog is a data leak between databases.
     */
    protected String catalog;
    /** Schema (or database / keyspace) that holds the CDC objects. */
    protected String schema;

    protected TriggerCdcInstallMode installMode;
    /** How often the framework polls the journal. */
    protected Long pollIntervalMs;
    /** How many change records a single read returns at most. */
    protected Integer batchSize;
    protected TriggerCdcCaptureImage captureImage;
    protected TriggerCdcDdlMode ddlMode;
    protected TriggerCdcErrorPolicy errorPolicy;
    /**
     * How long acknowledged change records are kept before cleanup. Retention must never beat
     * the acknowledgement rule: unacknowledged events are never deleted because of TTL alone.
     */
    protected Long retentionSeconds;
    /**
     * Explicit opt-in that lets cleanup reclaim journal space from records not every live
     * consumer has acknowledged. Defaults to {@code false} and must stay opt-in: deleting such a
     * record destroys the data of a consumer that never saw it, so it must never happen as a
     * silent side effect of a TTL.
     *
     * <p>When enabled, records older than {@link #retentionSeconds} may be removed even though
     * some live consumer still needs them, but the connector then has to (a) count them in
     * {@link TriggerCdcCleanupResult#getDeletedUnacknowledgedCount()}, (b) list every affected
     * consumer in {@link TriggerCdcCleanupResult#getIntegrityBrokenConsumers()} and (c) persist
     * that marking no later than the delete, so the framework can degrade or repair those tasks
     * instead of discovering the gap on its own. The framework enforces all three through
     * {@link TriggerCdcCleanupResult#validate(TriggerCdcConfig)}.</p>
     */
    protected Boolean forceCleanUnacknowledged;
    /**
     * Whether {@code TRUNCATE} is captured as its own event. Not every engine can observe it, and it
     * must never be expanded into row level DELETE events.
     *
     * <p>Must agree with the capability declaration: this option may only be honoured when the
     * connector reports {@link TriggerCdcCapabilities#isTruncateCaptureSupported()}, and a capture
     * reporting {@link TriggerCdcOperation#TRUNCATE} events has to be declared capable.</p>
     */
    protected Boolean captureTruncate;

    public static TriggerCdcConfig create() {
        return new TriggerCdcConfig();
    }

    /**
     * Fills every null field that has a documented default and returns {@code this}.
     *
     * <p>{@link #getCatalog()} is deliberately left untouched: it has no default, because the
     * database either has no catalog concept or the catalog was chosen explicitly, and guessing one
     * would write the journal into the wrong database.</p>
     *
     * <p>Callers should use this instead of relying on {@code null} meaning "default", so the
     * effective configuration can be logged and stored verbatim. After this call every field except
     * {@code catalog} is non null.</p>
     */
    public TriggerCdcConfig withDefaults() {
        if (schema == null) {
            schema = TriggerCdcConstants.DEFAULT_STORAGE_SCHEMA;
        }
        if (installMode == null) {
            installMode = TriggerCdcInstallMode.AUTO;
        }
        if (pollIntervalMs == null) {
            pollIntervalMs = TriggerCdcConstants.DEFAULT_POLL_INTERVAL_MS;
        }
        if (batchSize == null) {
            batchSize = TriggerCdcConstants.DEFAULT_BATCH_SIZE;
        }
        if (captureImage == null) {
            captureImage = TriggerCdcCaptureImage.FULL;
        }
        if (ddlMode == null) {
            ddlMode = TriggerCdcDdlMode.DETECT_ONLY;
        }
        if (errorPolicy == null) {
            errorPolicy = TriggerCdcErrorPolicy.STRICT;
        }
        if (retentionSeconds == null) {
            retentionSeconds = TriggerCdcConstants.DEFAULT_RETENTION_SECONDS;
        }
        if (forceCleanUnacknowledged == null) {
            forceCleanUnacknowledged = Boolean.FALSE;
        }
        if (captureTruncate == null) {
            captureTruncate = Boolean.FALSE;
        }
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public TriggerCdcConfig catalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TriggerCdcConfig schema(String schema) {
        this.schema = schema;
        return this;
    }

    public TriggerCdcInstallMode getInstallMode() {
        return installMode;
    }

    public void setInstallMode(TriggerCdcInstallMode installMode) {
        this.installMode = installMode;
    }

    public TriggerCdcConfig installMode(TriggerCdcInstallMode installMode) {
        this.installMode = installMode;
        return this;
    }

    public Long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(Long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public TriggerCdcConfig pollIntervalMs(Long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
        return this;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public TriggerCdcConfig batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public TriggerCdcCaptureImage getCaptureImage() {
        return captureImage;
    }

    public void setCaptureImage(TriggerCdcCaptureImage captureImage) {
        this.captureImage = captureImage;
    }

    public TriggerCdcConfig captureImage(TriggerCdcCaptureImage captureImage) {
        this.captureImage = captureImage;
        return this;
    }

    public TriggerCdcDdlMode getDdlMode() {
        return ddlMode;
    }

    public void setDdlMode(TriggerCdcDdlMode ddlMode) {
        this.ddlMode = ddlMode;
    }

    public TriggerCdcConfig ddlMode(TriggerCdcDdlMode ddlMode) {
        this.ddlMode = ddlMode;
        return this;
    }

    public TriggerCdcErrorPolicy getErrorPolicy() {
        return errorPolicy;
    }

    public void setErrorPolicy(TriggerCdcErrorPolicy errorPolicy) {
        this.errorPolicy = errorPolicy;
    }

    public TriggerCdcConfig errorPolicy(TriggerCdcErrorPolicy errorPolicy) {
        this.errorPolicy = errorPolicy;
        return this;
    }

    public Long getRetentionSeconds() {
        return retentionSeconds;
    }

    public void setRetentionSeconds(Long retentionSeconds) {
        this.retentionSeconds = retentionSeconds;
    }

    public TriggerCdcConfig retentionSeconds(Long retentionSeconds) {
        this.retentionSeconds = retentionSeconds;
        return this;
    }

    public Boolean getForceCleanUnacknowledged() {
        return forceCleanUnacknowledged;
    }

    public void setForceCleanUnacknowledged(Boolean forceCleanUnacknowledged) {
        this.forceCleanUnacknowledged = forceCleanUnacknowledged;
    }

    public TriggerCdcConfig forceCleanUnacknowledged(Boolean forceCleanUnacknowledged) {
        this.forceCleanUnacknowledged = forceCleanUnacknowledged;
        return this;
    }

    /**
     * Null safe reading of {@link #getForceCleanUnacknowledged()}: an unset value means
     * "not opted in", never "the connector may decide".
     */
    public boolean forceCleanUnacknowledgedEnabled() {
        return Boolean.TRUE.equals(forceCleanUnacknowledged);
    }

    public Boolean getCaptureTruncate() {
        return captureTruncate;
    }

    public void setCaptureTruncate(Boolean captureTruncate) {
        this.captureTruncate = captureTruncate;
    }

    public TriggerCdcConfig captureTruncate(Boolean captureTruncate) {
        this.captureTruncate = captureTruncate;
        return this;
    }
}

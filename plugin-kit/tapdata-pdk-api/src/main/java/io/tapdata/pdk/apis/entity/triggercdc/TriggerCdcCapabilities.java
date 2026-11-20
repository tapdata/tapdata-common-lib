package io.tapdata.pdk.apis.entity.triggercdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a specific connector can really do with trigger based CDC.
 *
 * <p>This is the contract that keeps the framework honest: "the database has triggers" never
 * implies "the connector supports full CDC". Every optional feature must be declared here and
 * the framework must not use a feature that is not declared.</p>
 *
 * <p>Declarations are per database <b>and version</b>: a connector must report the version it
 * validated against, because trigger behaviour and DDL visibility differ between releases.</p>
 */
public class TriggerCdcCapabilities implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Database type this declaration belongs to. */
    protected String databaseType;
    /** Database version the declaration was verified against. */
    protected String databaseVersion;
    /** Connector build that produced this declaration. */
    protected String connectorVersion;

    /**
     * Operations observable through the trigger. {@link TriggerCdcOperation#TRUNCATE} appears here
     * if and only if {@link #isTruncateCaptureSupported()} is {@code true}.
     */
    protected Set<TriggerCdcOperation> dmlOperations;
    /** Capture images the connector can materialise. */
    protected Set<TriggerCdcCaptureImage> captureImages;
    /** DDL levels the connector honours. */
    protected Set<TriggerCdcDdlMode> ddlModes;
    /** Error policies the connector implements. STRICT must always be present. */
    protected Set<TriggerCdcErrorPolicy> errorPolicies;

    /**
     * The transaction id is stored with each change record. Useful for dedup and for grouping
     * events of the same source transaction.
     */
    protected boolean transactionIdSupported;
    /** A per transaction sequence is stored, so the order inside one transaction is provable. */
    protected boolean transactionSequenceSupported;
    /**
     * The connector can prove a capture boundary across installation, so no change committed
     * while the trigger is being created is lost.
     *
     * <p>Necessary but insufficient for automatic full-then-incremental handover. The engine and
     * connector must also verify snapshot/journal overlap handling and durable recovery of the
     * snapshot and replay position. Until that protocol is implemented and verified, automatic
     * handover must remain disabled even when this flag is true. See docs/trigger-cdc.md.</p>
     */
    protected boolean captureBoundarySupported;
    /**
     * The database exposes a verified commit ordered cursor. Informational only.
     *
     * <p>It records what a future, versioned contract could exploit to replace per batch
     * acknowledgement with a high watermark. It must <b>not</b> be used to skip acknowledgement
     * under the current contract: the position is moved by
     * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcAcknowledgeFunction}
     * alone, and a connector that advances its own cursor claiming this flag is betting on a
     * guarantee the framework has not verified.</p>
     */
    protected boolean commitOrderedCursorSupported;
    /** Ordering of changes of the same business key is provable and preserved. */
    protected boolean perKeyOrderingSupported;
    /** Commit order across different tables of one source transaction is provable. */
    protected boolean crossTableOrderingSupported;
    /** A whole source transaction can be replayed atomically on the target. */
    protected boolean atomicReplaySupported;
    /** TRUNCATE can be observed as its own event; must match {@link TriggerCdcOperation#TRUNCATE}. */
    protected boolean truncateCaptureSupported;
    /** The connector can detect and build a safe starting point for newly created tables. */
    protected boolean autoDiscoverNewTableSupported;
    /** Several consumers may read the same journal without stealing each other's events. */
    protected boolean multiConsumerSupported;
    /** The journal survives a connector restart without losing events. */
    protected boolean durableJournal;
    /** The trigger can be installed and removed without taking a long exclusive table lock. */
    protected boolean onlineInstallSupported;

    /** Largest journal payload a single change may produce, in bytes. 0 means unknown. */
    protected long maxPayloadBytes;

    /**
     * Column types the connector refuses to capture, for example LOB or spatial types it cannot
     * round trip. Refusing at pre-check time is required; silently truncating is not.
     */
    protected List<String> unsupportedTypes;

    /** Connector specific notes, for example the minimum supported database version. */
    protected Map<String, String> extra;

    public static TriggerCdcCapabilities create() {
        return new TriggerCdcCapabilities();
    }

    public TriggerCdcCapabilities dmlOperation(TriggerCdcOperation operation) {
        if (dmlOperations == null) {
            dmlOperations = new LinkedHashSet<>();
        }
        dmlOperations.add(operation);
        return this;
    }

    public TriggerCdcCapabilities captureImage(TriggerCdcCaptureImage captureImage) {
        if (captureImages == null) {
            captureImages = new LinkedHashSet<>();
        }
        captureImages.add(captureImage);
        return this;
    }

    public TriggerCdcCapabilities ddlMode(TriggerCdcDdlMode ddlMode) {
        if (ddlModes == null) {
            ddlModes = new LinkedHashSet<>();
        }
        ddlModes.add(ddlMode);
        return this;
    }

    public TriggerCdcCapabilities errorPolicy(TriggerCdcErrorPolicy errorPolicy) {
        if (errorPolicies == null) {
            errorPolicies = new LinkedHashSet<>();
        }
        errorPolicies.add(errorPolicy);
        return this;
    }

    public TriggerCdcCapabilities unsupportedType(String unsupportedType) {
        if (unsupportedTypes == null) {
            unsupportedTypes = new ArrayList<>();
        }
        unsupportedTypes.add(unsupportedType);
        return this;
    }

    public boolean supports(TriggerCdcOperation operation) {
        return dmlOperations != null && dmlOperations.contains(operation);
    }

    public boolean supports(TriggerCdcCaptureImage captureImage) {
        return captureImages != null && captureImages.contains(captureImage);
    }

    public boolean supports(TriggerCdcDdlMode ddlMode) {
        return ddlModes != null && ddlModes.contains(ddlMode);
    }

    public boolean supports(TriggerCdcErrorPolicy errorPolicy) {
        return errorPolicies != null && errorPolicies.contains(errorPolicy);
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public TriggerCdcCapabilities databaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public TriggerCdcCapabilities databaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
        return this;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }

    public TriggerCdcCapabilities connectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
        return this;
    }

    public Set<TriggerCdcOperation> getDmlOperations() {
        return dmlOperations;
    }

    public void setDmlOperations(Set<TriggerCdcOperation> dmlOperations) {
        this.dmlOperations = dmlOperations;
    }

    public Set<TriggerCdcCaptureImage> getCaptureImages() {
        return captureImages;
    }

    public void setCaptureImages(Set<TriggerCdcCaptureImage> captureImages) {
        this.captureImages = captureImages;
    }

    public Set<TriggerCdcDdlMode> getDdlModes() {
        return ddlModes;
    }

    public void setDdlModes(Set<TriggerCdcDdlMode> ddlModes) {
        this.ddlModes = ddlModes;
    }

    public Set<TriggerCdcErrorPolicy> getErrorPolicies() {
        return errorPolicies;
    }

    public void setErrorPolicies(Set<TriggerCdcErrorPolicy> errorPolicies) {
        this.errorPolicies = errorPolicies;
    }

    public boolean isTransactionIdSupported() {
        return transactionIdSupported;
    }

    public void setTransactionIdSupported(boolean transactionIdSupported) {
        this.transactionIdSupported = transactionIdSupported;
    }

    public TriggerCdcCapabilities transactionIdSupported(boolean transactionIdSupported) {
        this.transactionIdSupported = transactionIdSupported;
        return this;
    }

    public boolean isTransactionSequenceSupported() {
        return transactionSequenceSupported;
    }

    public void setTransactionSequenceSupported(boolean transactionSequenceSupported) {
        this.transactionSequenceSupported = transactionSequenceSupported;
    }

    public TriggerCdcCapabilities transactionSequenceSupported(boolean transactionSequenceSupported) {
        this.transactionSequenceSupported = transactionSequenceSupported;
        return this;
    }

    public boolean isCaptureBoundarySupported() {
        return captureBoundarySupported;
    }

    public void setCaptureBoundarySupported(boolean captureBoundarySupported) {
        this.captureBoundarySupported = captureBoundarySupported;
    }

    public TriggerCdcCapabilities captureBoundarySupported(boolean captureBoundarySupported) {
        this.captureBoundarySupported = captureBoundarySupported;
        return this;
    }

    public boolean isCommitOrderedCursorSupported() {
        return commitOrderedCursorSupported;
    }

    public void setCommitOrderedCursorSupported(boolean commitOrderedCursorSupported) {
        this.commitOrderedCursorSupported = commitOrderedCursorSupported;
    }

    public TriggerCdcCapabilities commitOrderedCursorSupported(boolean commitOrderedCursorSupported) {
        this.commitOrderedCursorSupported = commitOrderedCursorSupported;
        return this;
    }

    public boolean isPerKeyOrderingSupported() {
        return perKeyOrderingSupported;
    }

    public void setPerKeyOrderingSupported(boolean perKeyOrderingSupported) {
        this.perKeyOrderingSupported = perKeyOrderingSupported;
    }

    public TriggerCdcCapabilities perKeyOrderingSupported(boolean perKeyOrderingSupported) {
        this.perKeyOrderingSupported = perKeyOrderingSupported;
        return this;
    }

    public boolean isCrossTableOrderingSupported() {
        return crossTableOrderingSupported;
    }

    public void setCrossTableOrderingSupported(boolean crossTableOrderingSupported) {
        this.crossTableOrderingSupported = crossTableOrderingSupported;
    }

    public TriggerCdcCapabilities crossTableOrderingSupported(boolean crossTableOrderingSupported) {
        this.crossTableOrderingSupported = crossTableOrderingSupported;
        return this;
    }

    public boolean isAtomicReplaySupported() {
        return atomicReplaySupported;
    }

    public void setAtomicReplaySupported(boolean atomicReplaySupported) {
        this.atomicReplaySupported = atomicReplaySupported;
    }

    public TriggerCdcCapabilities atomicReplaySupported(boolean atomicReplaySupported) {
        this.atomicReplaySupported = atomicReplaySupported;
        return this;
    }

    public boolean isTruncateCaptureSupported() {
        return truncateCaptureSupported;
    }

    public void setTruncateCaptureSupported(boolean truncateCaptureSupported) {
        this.truncateCaptureSupported = truncateCaptureSupported;
    }

    public TriggerCdcCapabilities truncateCaptureSupported(boolean truncateCaptureSupported) {
        this.truncateCaptureSupported = truncateCaptureSupported;
        return this;
    }

    public boolean isAutoDiscoverNewTableSupported() {
        return autoDiscoverNewTableSupported;
    }

    public void setAutoDiscoverNewTableSupported(boolean autoDiscoverNewTableSupported) {
        this.autoDiscoverNewTableSupported = autoDiscoverNewTableSupported;
    }

    public TriggerCdcCapabilities autoDiscoverNewTableSupported(boolean autoDiscoverNewTableSupported) {
        this.autoDiscoverNewTableSupported = autoDiscoverNewTableSupported;
        return this;
    }

    public boolean isMultiConsumerSupported() {
        return multiConsumerSupported;
    }

    public void setMultiConsumerSupported(boolean multiConsumerSupported) {
        this.multiConsumerSupported = multiConsumerSupported;
    }

    public TriggerCdcCapabilities multiConsumerSupported(boolean multiConsumerSupported) {
        this.multiConsumerSupported = multiConsumerSupported;
        return this;
    }

    public boolean isDurableJournal() {
        return durableJournal;
    }

    public void setDurableJournal(boolean durableJournal) {
        this.durableJournal = durableJournal;
    }

    public TriggerCdcCapabilities durableJournal(boolean durableJournal) {
        this.durableJournal = durableJournal;
        return this;
    }

    public boolean isOnlineInstallSupported() {
        return onlineInstallSupported;
    }

    public void setOnlineInstallSupported(boolean onlineInstallSupported) {
        this.onlineInstallSupported = onlineInstallSupported;
    }

    public TriggerCdcCapabilities onlineInstallSupported(boolean onlineInstallSupported) {
        this.onlineInstallSupported = onlineInstallSupported;
        return this;
    }

    public long getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    public void setMaxPayloadBytes(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }

    public TriggerCdcCapabilities maxPayloadBytes(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
        return this;
    }

    public List<String> getUnsupportedTypes() {
        return unsupportedTypes;
    }

    public void setUnsupportedTypes(List<String> unsupportedTypes) {
        this.unsupportedTypes = unsupportedTypes;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }
}

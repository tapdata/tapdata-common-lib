package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * How much of the row image is mirrored into the CDC journal.
 */
public enum TriggerCdcCaptureImage {
    /**
     * Default. INSERT records the full after image, UPDATE records the full before and after
     * images, DELETE records the full before image.
     *
     * <p>Full images are needed to support filter condition changes, primary key changes,
     * deletes and lookups on the target side. They cost more storage, but they never force a
     * later re-read of the source table, which would observe already updated data.</p>
     */
    FULL,
    /**
     * Only key columns plus changed columns are mirrored. Lower storage cost, but the target
     * side can no longer rebuild the previous / next full row state.
     *
     * <p>Only usable when a connector declares it in
     * {@link TriggerCdcCapabilities#getCaptureImages()}. Never assume it as a default.</p>
     */
    MINIMAL
}

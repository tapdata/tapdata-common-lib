package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * Who executes the generated CDC objects and triggers.
 */
public enum TriggerCdcInstallMode {
    /**
     * Default. The framework generates the statements and executes them itself, then verifies
     * the result. Installation must be idempotent and must never overwrite an object Tapdata
     * does not own.
     */
    AUTO,
    /**
     * The framework only generates the statements through
     * {@link io.tapdata.pdk.apis.functions.connector.triggercdc.TriggerCdcPlanFunction}; a DBA
     * installs them and Tapdata verifies the result afterwards. Both paths must produce exactly
     * the same object set and the same version.
     */
    MANUAL
}

package io.tapdata.pdk.apis.entity.triggercdc;

/**
 * Kind of DDL statement inside a {@link TriggerCdcInstallPlan}.
 */
public enum TriggerCdcStatementType {
    CREATE_NAMESPACE,
    CREATE_TABLE,
    CREATE_INDEX,
    CREATE_TRIGGER,
    DROP_TRIGGER,
    DROP_TABLE,
    DROP_NAMESPACE,
    OTHER
}

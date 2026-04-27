package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.ddl.table.TapAlterTableTTLEvent;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface AlterTableTTLFunction extends TapConnectorFunction {
    void alterTableTTL(TapConnectorContext connectorContext, TapAlterTableTTLEvent alterTableTTLEvent) throws Throwable;
}

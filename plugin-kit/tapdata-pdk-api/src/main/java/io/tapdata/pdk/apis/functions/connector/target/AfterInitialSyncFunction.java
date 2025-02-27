package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface AfterInitialSyncFunction extends TapConnectorFunction {
    void afterInitialSync(TapConnectorContext connectorContext, TapTable table) throws Throwable;
}

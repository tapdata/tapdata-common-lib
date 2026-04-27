package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface ConsumeMessageFunction extends TapConnectorFunction {
    void consumeMessage(TapConnectorContext connectorContext, TapTable tapTable, Long timestamp) throws Throwable;
}

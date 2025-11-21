package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface FlushOffsetFunction extends TapConnectorFunction {
    void flushOffset(TapConnectorContext connectorContext, Object offset);
}

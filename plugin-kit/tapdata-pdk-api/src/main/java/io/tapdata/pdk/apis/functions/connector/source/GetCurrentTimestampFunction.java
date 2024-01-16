package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface GetCurrentTimestampFunction extends TapConnectorFunction {
    long now(TapConnectorContext nodeContext) throws Throwable;
}

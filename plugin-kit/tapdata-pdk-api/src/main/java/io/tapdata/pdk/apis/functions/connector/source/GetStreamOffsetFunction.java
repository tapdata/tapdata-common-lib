package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface GetStreamOffsetFunction extends TapConnectorFunction {
    Object getStreamOffset(TapConnectorContext connectorContext, String offset);

}

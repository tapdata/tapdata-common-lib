package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.control.ControlEvent;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface ProcessControlFunction extends TapConnectorFunction {
    void processControl(TapConnectorContext connectorContext, ControlEvent controlEvent) throws Throwable;
}

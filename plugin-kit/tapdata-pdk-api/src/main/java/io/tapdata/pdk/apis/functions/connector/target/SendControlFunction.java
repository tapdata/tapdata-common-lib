package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.control.ControlEvent;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.ControlResult;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;
import java.util.function.Consumer;

public interface SendControlFunction extends TapConnectorFunction {
    void sendControl(TapConnectorContext connectorContext, List<ControlEvent> controlEvents, Consumer<ControlResult<ControlEvent>> consumer) throws Throwable;
}

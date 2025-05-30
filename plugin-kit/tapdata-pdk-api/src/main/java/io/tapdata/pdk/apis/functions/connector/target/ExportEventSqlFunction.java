package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

public interface ExportEventSqlFunction extends TapConnectorFunction {
    String exportEventSql(TapConnectorContext connectorContext, TapEvent tapEvent, TapTable table) throws Throwable;
}

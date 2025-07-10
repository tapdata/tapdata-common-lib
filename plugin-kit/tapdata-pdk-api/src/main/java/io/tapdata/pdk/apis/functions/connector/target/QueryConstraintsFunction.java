package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.schema.TapConstraint;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;
import java.util.function.Consumer;

public interface QueryConstraintsFunction extends TapConnectorFunction {
    void query(TapConnectorContext connectorContext, TapTable table, Consumer<List<TapConstraint>> consumer) throws Throwable;
}

package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.schema.TapConstraint;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;

import java.util.List;
import java.util.function.Consumer;

public interface QueryConstraintsFunction {
    void query(TapConnectorContext connectorContext, TapTable table, Consumer<List<TapConstraint<?>>> consumer) throws Throwable;
}

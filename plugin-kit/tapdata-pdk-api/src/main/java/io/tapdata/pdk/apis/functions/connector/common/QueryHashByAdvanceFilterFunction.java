package io.tapdata.pdk.apis.functions.connector.common;

import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.entity.TapAdvanceFilter;
import io.tapdata.pdk.apis.functions.connector.common.vo.TapHashResult;

import java.util.function.Consumer;

public interface QueryHashByAdvanceFilterFunction {
    public void query(TapConnectorContext connectorContext, TapAdvanceFilter filter, TapTable table, Consumer<TapHashResult<String>> consumer) throws Throwable;
}

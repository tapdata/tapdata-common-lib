package io.tapdata.pdk.apis.functions.connection;

import io.tapdata.entity.utils.DataMap;
import io.tapdata.pdk.apis.context.TapConnectionContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectionFunction;

import java.util.List;
import java.util.function.Consumer;

public interface ExecuteCommandV2Function extends TapConnectionFunction {
    /**
     * @param connectionContext the node context in a DAG
     */
    void execute(TapConnectionContext connectionContext, String sqlType, String sql, Consumer<List<DataMap>> consumer) throws Throwable;
}


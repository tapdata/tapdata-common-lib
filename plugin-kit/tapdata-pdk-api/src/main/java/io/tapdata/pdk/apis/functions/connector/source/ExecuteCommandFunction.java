package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.pdk.apis.context.TapConnectionContext;
import io.tapdata.pdk.apis.entity.ExecuteResult;
import io.tapdata.pdk.apis.entity.TapExecuteCommand;
import io.tapdata.pdk.apis.functions.connector.TapConnectionFunction;

import java.util.function.Consumer;

public interface ExecuteCommandFunction extends TapConnectionFunction {
    /**
     * @param connectionContext the node context in a DAG
     */
    void execute(TapConnectionContext connectionContext, TapExecuteCommand executeCommand, Consumer<ExecuteResult> consumer) throws Throwable;
}

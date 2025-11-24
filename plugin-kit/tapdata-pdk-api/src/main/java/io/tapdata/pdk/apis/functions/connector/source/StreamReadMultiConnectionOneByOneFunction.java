package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.pdk.apis.consumer.StreamReadOneByOneConsumer;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

public interface StreamReadMultiConnectionOneByOneFunction extends TapConnectorFunction {
    /**
     *
     * @param nodeContext the node context in a DAG
     * @param offsetState if null, means start from very beginning, otherwise is the start point for batch reading.
     *                    type can be any that comfortable for saving offset state.
     * @param consumer accept the table and offsetState for the record.
     */
    void streamRead(TapConnectorContext nodeContext, List<ConnectionConfigWithTables> connectionConfigWithTables, Object offsetState, StreamReadOneByOneConsumer consumer) throws Throwable;
}

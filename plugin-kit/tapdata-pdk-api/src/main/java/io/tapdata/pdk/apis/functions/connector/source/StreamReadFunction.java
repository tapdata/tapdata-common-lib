package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.pdk.apis.consumer.StreamReadConsumer;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.TapConnectorFunction;

import java.util.List;

public interface StreamReadFunction extends TapConnectorFunction {
    /**
     * For Trigger CDC, ensure first registers the node's consumer and captures. The offset is a
     * complete map keyed by capture id as specified by
     * {@link io.tapdata.pdk.apis.entity.triggercdc.TriggerCdcOffset}. Emit ordinary TapEvents and
     * a fresh offset snapshot; callback return does not authorize acknowledgement or cleanup.
     *
     * @param nodeContext the node context in a DAG
     * @param offsetState if null, means start from very beginning, otherwise is the start point for batch reading.
     *                    type can be any that comfortable for saving offset state.
     * @param recordSize
     * @param consumer accept the table and offsetState for the record.
     */
    void streamRead(TapConnectorContext nodeContext, List<String> tableList, Object offsetState, int recordSize, StreamReadConsumer consumer) throws Throwable;
}

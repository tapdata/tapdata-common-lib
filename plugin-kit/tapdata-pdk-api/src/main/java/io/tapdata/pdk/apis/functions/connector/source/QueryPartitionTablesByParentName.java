package io.tapdata.pdk.apis.functions.connector.source;

import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import io.tapdata.pdk.apis.functions.connector.common.vo.TapPartitionResult;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface QueryPartitionTablesByParentName {
    public void query(TapConnectorContext connectorContext, List<TapTable> table, Consumer<Collection<TapPartitionResult>> consumer) throws Exception;
}

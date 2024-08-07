package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.ddl.table.TapCreateTableEvent;
import io.tapdata.pdk.apis.context.TapConnectorContext;

public interface CreatPartitionTableFunction {
    //create partition table
    CreateTableOptions createTable(TapConnectorContext connectorContext, TapCreateTableEvent createTableEvent) throws Exception;
}

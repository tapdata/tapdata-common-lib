package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.ddl.table.TapCreateTableEvent;
import io.tapdata.pdk.apis.context.TapConnectorContext;

public interface CreatePartitionSubTableFunction {

    /**
     * @param connectorContext
     * @param masterTableEvent {
     *         tapTable: master table
     * }
     * @param subTableId
     * */
    CreateTableOptions createSubPartitionTable(TapConnectorContext connectorContext, TapCreateTableEvent masterTableEvent, String subTableId) throws Exception;
}

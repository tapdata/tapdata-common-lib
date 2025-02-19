package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.ddl.constraint.TapCreateConstraintEvent;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;

public interface CreateConstraintFunction {
    void createConstraint(TapConnectorContext connectorContext, TapTable table, TapCreateConstraintEvent createConstraintEvent, boolean create) throws Throwable;
}

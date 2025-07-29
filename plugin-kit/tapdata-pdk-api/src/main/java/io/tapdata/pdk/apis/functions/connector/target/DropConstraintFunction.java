package io.tapdata.pdk.apis.functions.connector.target;

import io.tapdata.entity.event.ddl.constraint.TapDropConstraintEvent;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.pdk.apis.context.TapConnectorContext;

public interface DropConstraintFunction {
    void dropConstraint(TapConnectorContext connectorContext, TapTable table, TapDropConstraintEvent tapDropConstraintEvent) throws Throwable;
}

package io.tapdata.entity.event.ddl.constraint;

import io.tapdata.entity.event.ddl.TapDDLEvent;

public abstract class TapConstraintEvent extends TapDDLEvent {
    public TapConstraintEvent(int type) {
        super(type);
    }
}

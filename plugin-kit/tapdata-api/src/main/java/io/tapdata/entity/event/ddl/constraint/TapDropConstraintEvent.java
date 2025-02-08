package io.tapdata.entity.event.ddl.constraint;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.entity.schema.TapConstraint;

import java.util.ArrayList;
import java.util.List;

public class TapDropConstraintEvent extends TapConstraintEvent {
    public static final int TYPE = 100;
    private List<TapConstraint> constraintList;

    public TapDropConstraintEvent() {
        super(TYPE);
    }

    public TapDropConstraintEvent constraintList(List<TapConstraint> constraintList) {
        this.constraintList = constraintList;
        return this;
    }

    public List<TapConstraint> getConstraintList() {
        return constraintList;
    }

    public void setConstraintList(List<TapConstraint> constraintList) {
        this.constraintList = constraintList;
    }

    @Override
    public void clone(TapEvent tapEvent) {
        super.clone(tapEvent);
        if (tapEvent instanceof TapDropConstraintEvent) {
            TapDropConstraintEvent tapDropConstraintEvent = (TapDropConstraintEvent) tapEvent;
            tapDropConstraintEvent.constraintList = new ArrayList<>(constraintList);
        }
    }
}

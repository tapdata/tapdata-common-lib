package io.tapdata.entity.event.ddl.constraint;

import io.tapdata.entity.event.TapEvent;
import io.tapdata.entity.schema.TapConstraint;

import java.util.ArrayList;
import java.util.List;

public class TapCreateConstraintEvent extends TapConstraintEvent {
    public static final int TYPE = 101;
    private List<TapConstraint> constraintList;
    private List<String> constraintSqlList;

    public TapCreateConstraintEvent() {
        super(TYPE);
    }

    public TapCreateConstraintEvent constraintList(List<TapConstraint> constraintList) {
        this.constraintList = constraintList;
        return this;
    }

    public TapCreateConstraintEvent constraintSqlList(List<String> constraintSqlList) {
        this.constraintSqlList = constraintSqlList;
        return this;
    }

    public List<TapConstraint> getConstraintList() {
        return constraintList;
    }

    public void setConstraintList(List<TapConstraint> constraintList) {
        this.constraintList = constraintList;
    }

    public List<String> getConstraintSqlList() {
        return constraintSqlList;
    }

    public void setConstraintSqlList(List<String> constraintSqlList) {
        this.constraintSqlList = constraintSqlList;
    }

    @Override
    public void clone(TapEvent tapEvent) {
        super.clone(tapEvent);
        if (tapEvent instanceof TapCreateConstraintEvent) {
            TapCreateConstraintEvent tapCreateConstraintEvent = (TapCreateConstraintEvent) tapEvent;
            tapCreateConstraintEvent.constraintList = new ArrayList<>(constraintList);
            tapCreateConstraintEvent.constraintSqlList = new ArrayList<>(constraintSqlList);
        }
    }
}

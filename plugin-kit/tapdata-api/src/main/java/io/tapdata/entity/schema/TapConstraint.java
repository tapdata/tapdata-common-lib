package io.tapdata.entity.schema;

import java.io.Serializable;

public abstract class TapConstraint<T extends TapConstraint<T>> implements Serializable {
    private String name;

    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

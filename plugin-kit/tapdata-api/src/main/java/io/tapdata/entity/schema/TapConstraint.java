package io.tapdata.entity.schema;

import java.io.Serializable;

public abstract class TapConstraint implements Serializable {
    private String name;
    public <T extends TapConstraint> T name(String name) {
        this.name = name;
        return (T)this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package io.tapdata.entity.schema.method;

import java.io.Serializable;

public abstract class TapMethod implements Serializable {
    String tapName;

    public TapMethod(String tapName) {
        this.tapName = tapName;
    }

    public TapMethod withTapName(String tapName) {
        this.tapName = tapName;
        return this;
    }

    public String getTapName() {
        return tapName;
    }

    public void setTapName(String tapName) {
        this.tapName = tapName;
    }
}

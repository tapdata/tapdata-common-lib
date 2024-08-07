package io.tapdata.entity.schema.partition;

import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.method.TapMethod;

import java.io.Serializable;

public class TapPartitionField extends TapField implements Serializable {
    private static final long serialVersionUID = 1L;

    TapMethod method;

    public TapMethod getMethod() {
        return method;
    }

    public void setMethod(TapMethod method) {
        this.method = method;
    }
}

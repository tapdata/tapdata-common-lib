package io.tapdata.pdk.apis.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ControlResult<T> {

    private boolean success;

    private Map<T, Throwable> errorMap;

    public ControlResult<T> addError(T key, Throwable value) {
        if (errorMap == null) {
            errorMap = new LinkedHashMap<>();
        }
        errorMap.put(key, value);
        return this;
    }

    public ControlResult() {
    }

    public Map<T, Throwable> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(Map<T, Throwable> errorMap) {
        this.errorMap = errorMap;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void addErrors(Map<T, Throwable> map) {
        if (errorMap == null) {
            errorMap = new HashMap<>();
        }
        errorMap.putAll(map);
    }
}

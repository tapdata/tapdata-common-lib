package io.tapdata.pdk.apis.functions.connector.common.vo;

public class TapHashResult<T> {
    protected T hash;
    private TapHashResult() { }

    public static TapHashResult<String> create() {
        return new TapHashResult<>();
    }

    public T getHash() {
        return hash;
    }

    public void setHash(T hash) {
        this.hash = hash;
    }

    public TapHashResult<T> withHash(T hash) {
        this.hash = hash;
        return this;
    }
}

package io.tapdata.pdk.apis.functions.connector.common.vo;

public class TapHashResult {
    protected Long hash;
    private TapHashResult() { }

    public static TapHashResult create() {
        return new TapHashResult();
    }

    public Long getHash() {
        return hash;
    }

    public void setHash(Long hash) {
        this.hash = hash;
    }

    public TapHashResult withHash(Long hash) {
        this.hash = hash;
        return this;
    }
}

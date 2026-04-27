package io.tapdata.pdk.apis.entity.message;

public class ConsumeParam {

    private String clientId;
    private Long timestamp;

    public static ConsumeParam create() {
        return new ConsumeParam();
    }

    public ConsumeParam() {

    }

    public ConsumeParam withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ConsumeParam withTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

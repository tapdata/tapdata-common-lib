package io.tapdata.entity.event.dml;

import io.tapdata.entity.event.TapBaseEvent;
import io.tapdata.entity.event.TapEvent;

import java.util.Collection;
import java.util.Map;

public abstract class TapRecordEvent extends TapBaseEvent {

    public static final String INFO_KEY_SYNC_STAGE = "SYNC_STAGE";
    /**
     * 数据源的类型， mysql一类
     */
    protected String connector;
    /**
     * 数据源的版本
     */
    protected String connectorVersion;
    protected boolean containsIllegalDate = false;

    public TapRecordEvent(int type) {
        super(type);
    }

    /*
    public void from(InputStream inputStream) throws IOException {
        super.from(inputStream);
        DataInputStreamEx dataInputStreamEx = dataInputStream(inputStream);
        connector = dataInputStreamEx.readUTF();
        connectorVersion = dataInputStreamEx.readUTF();
    }
    public void to(OutputStream outputStream) throws IOException {
        super.to(outputStream);
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        dataOutputStreamEx.writeUTF(connector);
        dataOutputStreamEx.writeUTF(connectorVersion);
    }
    */
    @Override
    public void clone(TapEvent tapEvent) {
        super.clone(tapEvent);
        if (tapEvent instanceof TapRecordEvent recordEvent) {
			recordEvent.connector = connector;
            recordEvent.connectorVersion = connectorVersion;
            recordEvent.exactlyOnceId = exactlyOnceId;
        }
    }

    public boolean getContainsIllegalDate() {
        return containsIllegalDate;
    }

    public void setContainsIllegalDate(boolean containsIllegalDate) {
        this.containsIllegalDate = containsIllegalDate;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }

    public abstract Map<String, Object> getFilter(Collection<String> primaryKeys);
//    @Override
//    public String toString() {
//        return "TapRecordEvent{" +
//                "connector='" + connector + '\'' +
//                ", connectorVersion='" + connectorVersion + '\'' +
//                "} " + super.toString();
//    }
}

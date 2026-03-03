package io.tapdata.pdk.apis.context;

import io.tapdata.entity.logger.Log;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.entity.utils.InstanceFactory;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.pdk.apis.spec.TapNodeSpecification;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TapConnectionContext extends TapContext {
    protected DataMap connectionConfig;
    protected DataMap nodeConfig;
    protected Map<String, DataMap> tableNodeConfig;
    protected Consumer<Object> flushOffsetCallback;

    public TapConnectionContext(TapNodeSpecification specification, DataMap connectionConfig, DataMap nodeConfig, Map<String, DataMap> tableNodeConfig, Log log) {
        super(specification);
        this.connectionConfig = connectionConfig;
        this.nodeConfig = nodeConfig;
        this.tableNodeConfig = tableNodeConfig;
        this.log = log;
    }

    public DataMap getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(DataMap connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public DataMap getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(DataMap nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public Map<String, DataMap> getTableNodeConfig() {
        return tableNodeConfig;
    }

    public void setTableNodeConfig(Map<String, DataMap> tableNodeConfig) {
        this.tableNodeConfig = tableNodeConfig;
    }

    public Consumer<Object> getFlushOffsetCallback() {
        return flushOffsetCallback;
    }

    public void setFlushOffsetCallback(Consumer<Object> flushOffsetCallback) {
        this.flushOffsetCallback = flushOffsetCallback;
    }

    public String toString() {
        return "TapConnectionContext connectionConfig: " + (connectionConfig != null ? Objects.requireNonNull(InstanceFactory.instance(JsonParser.class)).toJson(connectionConfig) : "") + "nodeConfig: " + (nodeConfig != null ? Objects.requireNonNull(InstanceFactory.instance(JsonParser.class)).toJson(nodeConfig) : "") + "tableNodeConfig: " + (tableNodeConfig != null ? Objects.requireNonNull(InstanceFactory.instance(JsonParser.class)).toJson(tableNodeConfig) : "") + " spec: " + specification + " id: " + id;
    }
}

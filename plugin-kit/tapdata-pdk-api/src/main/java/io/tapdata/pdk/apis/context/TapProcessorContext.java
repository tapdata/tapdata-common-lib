package io.tapdata.pdk.apis.context;

import io.tapdata.entity.logger.Log;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.pdk.apis.spec.TapNodeSpecification;

import java.util.Map;

public class TapProcessorContext extends TapContext {
    private DataMap nodeConfig;
    private Map<String, DataMap> tableNodeConfig;

    public TapProcessorContext(TapNodeSpecification specification, DataMap nodeConfig, Map<String, DataMap> tableNodeConfig, Log log) {
        super(specification);
        this.nodeConfig = nodeConfig;
        this.tableNodeConfig = tableNodeConfig;
        this.log = log;
    }

    public TapProcessorContext(TapNodeSpecification specification, DataMap nodeConfig, Log log) {
        super(specification);
        this.nodeConfig = nodeConfig;
        this.log = log;
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
}

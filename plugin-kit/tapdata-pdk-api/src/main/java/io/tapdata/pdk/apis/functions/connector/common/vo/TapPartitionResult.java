package io.tapdata.pdk.apis.functions.connector.common.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TapPartitionResult {
    String masterTableName;
    List<String> subPartitionTableNames;
    Map<String, Object> attr;


    public static TapPartitionResult create(String masterTableName) {
        return new TapPartitionResult().masterTableName(masterTableName);
    }

    public TapPartitionResult masterTableName(String masterTableName) {
        this.masterTableName = masterTableName;
        return this;
    }

    public TapPartitionResult addSubTable(String subTableName) {
        if (null == subTableName) return this;
        if (null == subPartitionTableNames) subPartitionTableNames = new ArrayList<>();
        this.subPartitionTableNames.add(subTableName);
        return this;
    }

    public TapPartitionResult addAllSubTable(Collection<String> subTableName) {
        if (null == subTableName || subTableName.isEmpty()) return this;
        if (null == subPartitionTableNames) subPartitionTableNames = new ArrayList<>();
        this.subPartitionTableNames.addAll(subTableName);
        return this;
    }

    public String getMasterTableName() {
        return masterTableName;
    }

    public void setMasterTableName(String masterTableName) {
        this.masterTableName = masterTableName;
    }

    public List<String> getSubPartitionTableNames() {
        return subPartitionTableNames;
    }

    public void setSubPartitionTableNames(List<String> subPartitionTableNames) {
        this.subPartitionTableNames = subPartitionTableNames;
    }

    public Map<String, Object> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, Object> attr) {
        this.attr = attr;
    }
}

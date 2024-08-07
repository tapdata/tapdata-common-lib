package io.tapdata.entity.schema.partition;

import io.tapdata.entity.schema.partition.type.TapPartitionType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapSubPartitionTableInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**Sub partition table name*/
    String tableName;

    /**Sub partition bound type*/
    List<TapPartitionType> tapPartitionTypes;

    String originPartitionBoundSQL;

    Map<String, Object> attr;

    public static TapSubPartitionTableInfo create() {
        return new TapSubPartitionTableInfo();
    }

    public String getOriginPartitionBoundSQL() {
        return originPartitionBoundSQL;
    }

    public void setOriginPartitionBoundSQL(String originPartitionBoundSQL) {
        this.originPartitionBoundSQL = originPartitionBoundSQL;
    }

    public TapSubPartitionTableInfo originPartitionBoundSQL(String originPartitionBoundSQL) {
        this.originPartitionBoundSQL = originPartitionBoundSQL;
        return this;
    }

    public TapSubPartitionTableInfo tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TapSubPartitionTableInfo partitionType(List<TapPartitionType> partitionType) {
        this.tapPartitionTypes = partitionType;
        return this;
    }

    public TapSubPartitionTableInfo attr(Map<String, Object> attr) {
        this.attr = attr;
        return this;
    }

    public TapSubPartitionTableInfo attr(String key, String value) {
        if (null == this.attr) this.attr = new HashMap<>();
        this.attr.put(key, value);
        return this;
    }

    public void removeAttr(String key) {
        if (null == this.attr) {
            return;
        }
        this.attr.remove(key);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, Object> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, Object> attr) {
        this.attr = attr;
    }

    public List<TapPartitionType> getTapPartitionTypes() {
        return tapPartitionTypes;
    }

    public void setTapPartitionTypes(List<TapPartitionType> tapPartitionTypes) {
        this.tapPartitionTypes = tapPartitionTypes;
    }
}

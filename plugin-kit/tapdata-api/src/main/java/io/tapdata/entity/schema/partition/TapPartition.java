package io.tapdata.entity.schema.partition;


import io.tapdata.entity.schema.partition.type.TapPartitionStage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TapPartition implements Serializable {
    private static final long serialVersionUID = 1L;

    TapPartitionStage type;

    String originPartitionStageSQL;

    List<TapPartitionField> partitionFields;

    List<TapSubPartitionTableInfo> subPartitionTableInfo;

    boolean invalidType;
    String invalidMsg;

    public static TapPartition create() {
        return new TapPartition();
    }

    public static TapPartition create(TapPartitionStage type, List<TapPartitionField> partitionFields) {
        return create().type(type).partitionFields(partitionFields);
    }

    public TapPartition originPartitionStageSQL(String originPartitionStageSQL) {
        this.originPartitionStageSQL = originPartitionStageSQL;
        return this;
    }

    public TapPartition type(TapPartitionStage type) {
        this.type = type;
        return this;
    }

    public TapPartition partitionFields(List<TapPartitionField> partitionFields) {
        this.partitionFields = partitionFields;
        return this;
    }

    public TapPartition addAllPartitionFields(List<TapPartitionField> partitionField) {
        if (null == partitionField || partitionField.isEmpty()) return this;
        if (null == this.partitionFields) this.partitionFields = new ArrayList<>();
        this.partitionFields.addAll(partitionField);
        return this;
    }

    public TapPartition partitionSchemas(List<TapSubPartitionTableInfo> partitionSchemas) {
        this.subPartitionTableInfo = partitionSchemas;
        return this;
    }

    public TapPartition appendPartitionSchemas(TapSubPartitionTableInfo partitionSchema) {
        if (null == this.subPartitionTableInfo) this.subPartitionTableInfo = new ArrayList<>();
        this.subPartitionTableInfo.add(partitionSchema);
        return this;
    }

    public TapPartitionStage getType() {
        return type;
    }

    public void setType(TapPartitionStage type) {
        this.type = type;
    }

    public List<TapPartitionField> getPartitionFields() {
        return partitionFields;
    }

    public void setPartitionFields(List<TapPartitionField> partitionFields) {
        this.partitionFields = partitionFields;
    }

    public List<TapSubPartitionTableInfo> getSubPartitionTableInfo() {
        return subPartitionTableInfo;
    }

    public void setSubPartitionTableInfo(List<TapSubPartitionTableInfo> subPartitionTableInfo) {
        this.subPartitionTableInfo = subPartitionTableInfo;
    }

    public String getOriginPartitionStageSQL() {
        return originPartitionStageSQL;
    }

    public void setOriginPartitionStageSQL(String originPartitionStageSQL) {
        this.originPartitionStageSQL = originPartitionStageSQL;
    }

    public boolean isInvalidType() {
        return invalidType;
    }

    public void setInvalidType(boolean invalidType) {
        this.invalidType = invalidType;
    }

    public String getInvalidMsg() {
        return invalidMsg;
    }

    public void setInvalidMsg(String invalidMsg) {
        this.invalidMsg = invalidMsg;
    }
}

package io.tapdata.entity.schema.partition.type;

public enum TapPartitionStage {
    HASH(TapPartitionHash.class),
    LIST(TapPartitionList.class),
    RANGE(TapPartitionRange.class),
    INHERIT(TapPartitionInherit.class);

    Class<? extends TapPartitionType> type;

    TapPartitionStage(Class<? extends TapPartitionType> type) {
        this.type = type;
    }

    public Class<? extends TapPartitionType> getType() {
        return type;
    }

    public void setType(Class<? extends TapPartitionType> type) {
        this.type = type;
    }
}
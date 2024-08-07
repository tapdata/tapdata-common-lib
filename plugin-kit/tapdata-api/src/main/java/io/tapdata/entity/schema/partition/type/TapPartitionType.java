package io.tapdata.entity.schema.partition.type;

public abstract class TapPartitionType {
    public static final String KEY_NAME = "type";

    public static final String RANGE = "RANGE";
    public static final String HASH = "HASH";
    public static final String LIST = "LIST";
    public static final String INHERIT = "INHERIT";
    private String type;

    public static Class<? extends TapPartitionType> getTapPartitionTypeClass(String type) {
        switch (type) {
            case RANGE:
                return TapPartitionRange.class;
            case HASH:
                return TapPartitionHash.class;
            case LIST:
                return TapPartitionList.class;
            default:
                return TapPartitionInherit.class;
        }
    }

    public TapPartitionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package io.tapdata.entity.schema.partition.type;

import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public abstract class TapPartitionType  implements Serializable, JavaCustomSerializer {
    private static final long serialVersionUID = 1L;
    public static final String KEY_NAME = "type";

    public static final String RANGE = "RANGE";
    public static final String HASH = "HASH";
    public static final String LIST = "LIST";
    public static final String INHERIT = "INHERIT";

    protected String type;
    protected String fieldName;

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

    public TapPartitionType() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        type = dataInputStream.original().readUTF();
        fieldName = dataInputStream.original().readUTF();
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        dataOutputStreamEx.original().writeUTF(type);
        dataOutputStreamEx.original().writeUTF(fieldName);
    }
}

package io.tapdata.entity.schema.value;

import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ByteData implements Serializable, JavaCustomSerializer {

    private byte[] value;
    private byte type;

    public ByteData() {
    }

    public ByteData(byte[] value) {
        this.value = value;
    }

    public ByteData(byte type, byte[] value) {
        this.type = type;
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    public byte getType() {
        return type;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        type = dataInputStream.readByte();
        value = dataInputStream.readBytes();
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        dataOutputStreamEx.write(type);
        dataOutputStreamEx.write(value);
    }

    @Override
    public DataInputStreamEx dataInputStream(InputStream inputStream) {
        return JavaCustomSerializer.super.dataInputStream(inputStream);
    }

    @Override
    public DataOutputStreamEx dataOutputStream(OutputStream outputStream) {
        return JavaCustomSerializer.super.dataOutputStream(outputStream);
    }
}

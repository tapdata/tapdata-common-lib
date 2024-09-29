package io.tapdata.entity.schema.partition.type;

import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * HASH Partitioning
 *
 * HASH 分区方法通过将数据的哈希值对指定的 modulus 取模，然后根据 remainder 决定数据分配到哪个分区
 * */
public class TapPartitionHash extends TapPartitionType implements Serializable {
    private static final long serialVersionUID = 1L;
    int modulus;
    int remainder;

    public TapPartitionHash() {
        super(HASH);
    }

    public TapPartitionHash modulus(int modulus) {
        this.modulus = modulus;
        return this;
    }

    public TapPartitionHash remainder(int remainder) {
        this.remainder = remainder;
        return this;
    }

    public int getModulus() {
        return modulus;
    }

    public void setModulus(int modulus) {
        this.modulus = modulus;
    }

    public int getRemainder() {
        return remainder;
    }

    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        super.from(inputStream);
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        modulus = dataInputStream.original().readInt();
        remainder = dataInputStream.original().readInt();
    }

}

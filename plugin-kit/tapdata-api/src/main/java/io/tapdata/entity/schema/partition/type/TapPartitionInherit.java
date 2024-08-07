package io.tapdata.entity.schema.partition.type;

import io.tapdata.entity.serializer.JavaCustomSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Inherit Partitioning
 *
 * 继承方式分区
 * */
public class TapPartitionInherit extends TapPartitionType implements Serializable, JavaCustomSerializer {
    private static final long serialVersionUID = 1L;

    public TapPartitionInherit() {
        super(INHERIT);
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
    }
}

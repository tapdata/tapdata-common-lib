package io.tapdata.entity.schema.partition.type;

import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.InstanceFactory;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.entity.utils.TypeHolder;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * RANGE Partitioning
 * */
public class TapPartitionRange<T> extends TapPartitionType implements Serializable {
    private static final long serialVersionUID = 1L;

    TapRangeValue<T> rangeFrom;
    TapRangeValue<T> rangeTo;

    public TapPartitionRange() {
        super(RANGE);
    }

    public TapPartitionRange<T> from(TapRangeValue<T> from) {
        this.rangeFrom = from;
        return this;
    }

    public TapPartitionRange<T> to(TapRangeValue<T> to) {
        this.rangeTo = to;
        return this;
    }

    public TapRangeValue<T> getRangeFrom() {
        return rangeFrom;
    }

    public void setRangeFrom(TapRangeValue<T> rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public TapRangeValue<T> getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(TapRangeValue<T> rangeTo) {
        this.rangeTo = rangeTo;
    }


    @Override
    public void from(InputStream inputStream) throws IOException {
        super.from(inputStream);
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        JsonParser instance = InstanceFactory.instance(JsonParser.class);
        rangeFrom = instance.fromJson(dataInputStream.original().readUTF(), new TypeHolder<TapRangeValue<T>>(){});
        rangeTo = instance.fromJson(dataInputStream.original().readUTF(), new TypeHolder<TapRangeValue<T>>(){});
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        super.to(outputStream);
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        JsonParser instance = InstanceFactory.instance(JsonParser.class);
        dataOutputStreamEx.original().writeUTF(instance.toJson(rangeFrom));
        dataOutputStreamEx.original().writeUTF(instance.toJson(rangeTo));
    }
}

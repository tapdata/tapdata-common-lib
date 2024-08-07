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
import java.util.List;

/**
 * LIST Partitioning
 * */
public class TapPartitionList<T> extends TapPartitionType implements Serializable, JavaCustomSerializer {
    private static final long serialVersionUID = 1L;

    boolean defaultList = false;
    List<T> listIn;

    public TapPartitionList() {
        super(LIST);
    }

    public TapPartitionList<T> setToDefault() {
        this.defaultList = true;
        return this;
    }

    public TapPartitionList<T> dataIn(List<T> listIn) {
        this.listIn = listIn;
        return this;
    }

    public boolean isDefaultList() {
        return defaultList;
    }

    public void setDefaultList(boolean defaultList) {
        this.defaultList = defaultList;
    }

    public List<T> getListIn() {
        return listIn;
    }

    public void setListIn(List<T> listIn) {
        this.listIn = listIn;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        defaultList = dataInputStream.original().readBoolean();
        listIn = InstanceFactory.instance(JsonParser.class).fromJson(dataInputStream.original().readUTF(), new TypeHolder<List<T>>(){});
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        dataOutputStreamEx.original().writeBoolean(defaultList);
        dataOutputStreamEx.original().writeUTF(InstanceFactory.instance(JsonParser.class).toJson(listIn));
    }
}

package io.tapdata.entity.schema.partition.type;

import io.tapdata.entity.serializer.JavaCustomSerializer;
import io.tapdata.entity.utils.InstanceFactory;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.entity.utils.io.DataInputStreamEx;
import io.tapdata.entity.utils.io.DataOutputStreamEx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TapRangeValue<T> implements Serializable, JavaCustomSerializer {
    private static final long serialVersionUID = 1L;

    ValueType valueType;
    T value;
    String originValue;

    public TapRangeValue() {
    }

    public TapRangeValue(T value, ValueType valueType, String originValue) {
        this.value = value;
        this.valueType = valueType;
        this.originValue = originValue;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getOriginValue() {
        return originValue;
    }

    public void setOriginValue(String originValue) {
        this.originValue = originValue;
    }

    @Override
    public void from(InputStream inputStream) throws IOException {
        DataInputStreamEx dataInputStream = dataInputStream(inputStream);
        String valueTypeName = dataInputStream.original().readUTF();
        try {
            valueType = ValueType.valueOf(valueTypeName);
        } catch (Exception e) {
            valueType = ValueType.NORMAL;
        }
        JsonParser instance = InstanceFactory.instance(JsonParser.class);
        Map<String, T> map = (Map<String, T>)instance.fromJson(dataInputStream.original().readUTF(), Map.class);
        value = map.get("ket");
        originValue = dataInputStream.original().readUTF();
    }

    @Override
    public void to(OutputStream outputStream) throws IOException {
        DataOutputStreamEx dataOutputStreamEx = dataOutputStream(outputStream);
        JsonParser instance = InstanceFactory.instance(JsonParser.class);
        Map<String, T> valueMap = new HashMap<>();
        valueMap.put("key", this.value);
        dataOutputStreamEx.original().writeUTF(valueType.name());
        dataOutputStreamEx.original().writeUTF(instance.toJson(valueMap));
        dataOutputStreamEx.original().writeUTF(originValue);
    }

    public static enum ValueType {
        MIN,
        MAX,
        NORMAL;
    }
}
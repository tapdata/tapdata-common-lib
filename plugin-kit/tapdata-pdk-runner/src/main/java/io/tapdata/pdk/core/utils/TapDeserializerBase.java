package io.tapdata.pdk.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public abstract class TapDeserializerBase<T> implements ObjectDeserializer {

    String key;

    public TapDeserializerBase(String key) {
        this.key = key;
    }

    public abstract Class<? extends T> getByType(String typeObj);

    @Override
    public T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject jsonObject = parser.parseObject(JSONObject.class);
        String typeStr = jsonObject.getString(key);
        return jsonObject.toJavaObject(getByType(typeStr));
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
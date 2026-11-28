package io.tapdata.pdk.core.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/** Uses the full numeric payload instead of the legacy Double getter. */
final class TapNumberValueJsonCodec implements ObjectSerializer, ObjectDeserializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }
        TapNumberValue value = (TapNumberValue) object;
        Number number = value.getNumberValue();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("originValue", value.getOriginValue());
        fields.put("originType", value.getOriginType());
        fields.put("tapType", value.getTapType());
        if (number == null || number instanceof Double) {
            fields.put("value", number);
            // JSON cannot represent non-finite values as numeric literals.
            if (number != null && (!Double.isFinite(number.doubleValue())
                    || Double.doubleToRawLongBits(number.doubleValue()) == Double.doubleToRawLongBits(-0.0d))) {
                fields.put("value", java.util.Collections.singletonMap("number", number.toString()));
                fields.put("numberType", "Double");
            }
        } else {
            String type = number.getClass().getSimpleName();
            fields.put("value", java.util.Collections.singletonMap("number", number.toString()));
            fields.put("numberType", type);
        }
        serializer.out.write('{');
        boolean comma = false;
        if (serializer.isEnabled(com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName)) {
            serializer.out.writeFieldName("@type");
            serializer.write(TapNumberValue.class.getName());
            comma = true;
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getValue() == null && !serializer.isEnabled(com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue))
                continue;
            if (comma) serializer.out.write(',');
            serializer.out.writeFieldName(entry.getKey());
            serializer.write(entry.getValue());
            comma = true;
        }
        serializer.out.write('}');
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        // The declared type is already known. Ignore this object's @type while
        // collecting fields, otherwise Fastjson redirects recursively to this codec.
        boolean disabled = parser.lexer.isEnabled(com.alibaba.fastjson.parser.Feature.DisableSpecialKeyDetect);
        JSONObject fields;
        parser.lexer.config(com.alibaba.fastjson.parser.Feature.DisableSpecialKeyDetect, true);
        try {
            fields = parser.parseObject();
        } finally {
            parser.lexer.config(com.alibaba.fastjson.parser.Feature.DisableSpecialKeyDetect, disabled);
        }
        if (fields == null) return null;
        String numberType = fields.getString("numberType");
        JSONObject encoded = numberType == null ? null : fields.getJSONObject("value");
        if (numberType != null && (encoded == null || encoded.getString("number") == null))
            throw new com.alibaba.fastjson.JSONException("Missing value.number for numeric representation " + numberType);
        Number number = numberType == null ? fields.getDouble("value")
                : parse(numberType, encoded.getString("number"));
        TapNumberValue value = TapNumberValue.ofNumber(number);
        value.setOriginValue(fields.get("originValue"));
        value.setOriginType(fields.getString("originType"));
        JSONObject tapType = fields.getJSONObject("tapType");
        if (tapType != null) {
            // Use a fixed allowlist; never instantiate arbitrary @type values.
            Class<? extends TapNumber> tapClass = TapNumber.class;
            String className = tapType.getString("@type");
            if (io.tapdata.entity.schema.type.TapFloat.class.getName().equals(className)
                    || "TapFloat".equals(tapType.getString("typeName")))
                tapClass = io.tapdata.entity.schema.type.TapFloat.class;
            else if (io.tapdata.entity.schema.type.TapDouble.class.getName().equals(className)
                    || "TapDouble".equals(tapType.getString("typeName")))
                tapClass = io.tapdata.entity.schema.type.TapDouble.class;
            else if (io.tapdata.entity.schema.type.TapCoefficientFloat.class.getName().equals(className)
                    || "TapCoefficientFloat".equals(tapType.getString("typeName")))
                tapClass = io.tapdata.entity.schema.type.TapCoefficientFloat.class;
            tapType.remove("@type");
            value.setTapType(tapType.toJavaObject(tapClass));
        }
        return (T) value;
    }

    private static Number parse(String type, String value) {
        if (value == null) return null;
        switch (type) {
            case "BigDecimal": return new BigDecimal(value);
            case "BigInteger": return new BigInteger(value);
            case "Long": return Long.valueOf(value);
            case "Integer": return Integer.valueOf(value);
            case "Short": return Short.valueOf(value);
            case "Byte": return Byte.valueOf(value);
            case "Float": return Float.valueOf(value);
            case "Double": return Double.valueOf(value);
            default: throw new IllegalArgumentException("Unsupported numeric representation: " + type);
        }
    }

    @Override
    public int getFastMatchToken() {
        return com.alibaba.fastjson.parser.JSONToken.LBRACE;
    }
}

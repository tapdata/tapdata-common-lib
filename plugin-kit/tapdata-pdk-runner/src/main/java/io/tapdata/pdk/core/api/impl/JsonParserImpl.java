package io.tapdata.pdk.core.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.tapdata.entity.schema.value.TapNumberValue;
import io.tapdata.entity.annotations.Implementation;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.entity.utils.InstanceFactory;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.entity.utils.TypeHolder;
import io.tapdata.pdk.core.utils.TapConstants;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Implementation(JsonParser.class)
public class JsonParserImpl implements JsonParser {
    // Exact-number adapters are scoped to this service, never installed in global
    // Fastjson configuration shared by unrelated connectors.
    private final SerializeConfig numberSerializeConfig = new SerializeConfig();
    private final ParserConfig numberParserConfig = newNumberParserConfig();
    private static final String TAG = JsonParserImpl.class.getSimpleName();

    private static ParserConfig newNumberParserConfig() {
        // Keep the numeric adapter local, but consult live host registrations before
        // local bean caches. Hosts can register/replace adapters after service creation.
        ParserConfig config = new ParserConfig() {
            @Override
            public Class<?> checkAutoType(String typeName, Class<?> expectClass, int features) {
                // Fastjson also consults its local registry when resolving @type.
                // Mirror only the requested host registration, then retain the normal
                // auto-type checks (including safe mode and deny lists).
                ParserConfig global = ParserConfig.getGlobalInstance();
                Class<?> registered = global.getDeserializers().findClass(typeName);
                if (registered != null && registered != TapNumberValue.class)
                    putDeserializer(registered, global.getDeserializers().get(registered));
                return super.checkAutoType(typeName, expectClass, features);
            }

            @Override
            public ObjectDeserializer getDeserializer(Type type) {
                if (type == TapNumberValue.class) return super.getDeserializer(type);
                ObjectDeserializer registered = ParserConfig.getGlobalInstance().getDeserializers().get(type);
                return registered != null ? registered : super.getDeserializer(type);
            }

            @Override
            public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
                if (clazz == TapNumberValue.class) return super.getDeserializer(clazz, type);
                ObjectDeserializer registered = ParserConfig.getGlobalInstance().getDeserializers().get(type);
                if (registered == null)
                    registered = ParserConfig.getGlobalInstance().getDeserializers().get(clazz);
                return registered != null ? registered : super.getDeserializer(clazz, type);
            }
        };
        config.putDeserializer(TapNumberValue.class, new TapNumberValueJsonCodec());
        return config;
    }

    public JsonParser configGlobalAbstractClassDetectors(Class<?> type, List<AbstractClassDetector> abstractClassDetectors) {
        ParserConfig.global.putDeserializer(type, new AbstractResultDeserializer(abstractClassDetectors));
        numberParserConfig.putDeserializer(type, new AbstractResultDeserializer(abstractClassDetectors));
        return this;
    }

    public JsonParserImpl() {
        configGlobalAbstractClassDetectors(TapType.class, TapConstants.abstractClassDetectors);
        ParserConfig.global.putDeserializer(Instant.class, new InstantDeserialize());
        numberParserConfig.putDeserializer(Instant.class, new InstantDeserialize());
        TapNumberValueJsonCodec numberCodec = new TapNumberValueJsonCodec();
        numberSerializeConfig.put(TapNumberValue.class, numberCodec);
        numberParserConfig.putDeserializer(TapNumberValue.class, numberCodec);
    }

    @Override
    public String toJsonWithClass(Object obj) {
        return JSON.toJSONString(obj, numberSerializeConfig, SerializerFeature.WriteClassName, SerializerFeature.DisableCircularReferenceDetect/*, SerializerFeature.SortField, SerializerFeature.MapSortField*/);
//        return PREFIX + obj.getClass().getName() + SUFFIX + JSON.toJSONString(obj);
    }

    @Override
    public Object fromJsonWithClass(String json) {
        return fromJsonWithClass(json, null);
    }

    @Override
    public Object fromJsonWithClass(String json, ClassLoader classLoader) {
        ParserConfig parserConfig = newNumberParserConfig();
        if (classLoader != null)
            parserConfig.setDefaultClassLoader(classLoader);
        return JSON.parseObject(json, DataMap.class, parserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
//        if(json.startsWith(PREFIX)) {
//            int pos = json.indexOf(SUFFIX);
//            if(pos > 0) {
//                String classStr = json.substring(PREFIX.length(), pos);
//                if(classLoader != null) {
//                    try {
//                        Class<?> clazz = classLoader.loadClass(classStr);
//                        return JSON.parseObject(json.substring(pos + SUFFIX.length()), clazz);
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        return null;
    }

    @Override
    public String toJson(Object obj, ToJsonFeature... features) {
        List<SerializerFeature> features1 = new ArrayList<>();;
        features1.add(SerializerFeature.DisableCircularReferenceDetect);
        if(features != null) {
            for(ToJsonFeature feature : features) {
                switch (feature) {
                    case PrettyFormat:
                        features1.add(SerializerFeature.PrettyFormat);
                        break;
                    case WriteMapNullValue:
                        features1.add(SerializerFeature.WriteMapNullValue);
                        break;
                    default:
                        TapLogger.warn(TAG, "Unsupported ToJsonFeature {}", feature);
                        break;
                }
            }
        }
        SerializerFeature[] serializerFeatures = new SerializerFeature[features1.size()];
        features1.toArray(serializerFeatures);
        return JSON.toJSONString(obj, numberSerializeConfig, serializerFeatures);
//        if (features != null && features.length > 0) {
//            //XXX to force adding WriteMapNullValue feature as the only one we have.
//            return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue/*, SerializerFeature.SortField, SerializerFeature.MapSortField*/);
//        } else {
//            return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect/*, SerializerFeature.SortField, SerializerFeature.MapSortField*/);
//        }
    }

    @Override
    public byte[] toJsonBytes(Object obj, ToJsonFeature... features) {
        if (features != null && features.length > 0) {
            //XXX to force adding WriteMapNullValue feature as the only one we have.
            return JSON.toJSONBytes(obj, numberSerializeConfig, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue/*, SerializerFeature.SortField, SerializerFeature.MapSortField*/);
        } else {
            return JSON.toJSONBytes(obj, numberSerializeConfig, SerializerFeature.DisableCircularReferenceDetect/*, SerializerFeature.SortField, SerializerFeature.MapSortField*/);
        }
    }

    @Override
    public <T> T fromJsonBytes(byte[] jsonBytes, Class<T> clazz) {
        return JSON.parseObject(jsonBytes, java.nio.charset.StandardCharsets.UTF_8, clazz, numberParserConfig, null, JSON.DEFAULT_PARSER_FEATURE, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    @Override
    public DataMap fromJsonObject(String json) {
        return JSON.parseObject(json, DataMap.class, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }
    @Override
    public List<?> fromJsonArray(String json) {
        return JSON.parseObject(json, List.class, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    @Override
    public Object fromJson(String json) {
        return JSON.parseObject(json, Object.class, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    @Override
    public <T> T fromJson(String json, Type clazz) {
        return JSON.parseObject(json, clazz, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }
//    @Override
//    public <T> T fromJson(String json, Class<T> clazz) {
//        return JSON.parseObject(json, clazz, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
//    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    /**
     * Recommend to use JsonParser#configGlobalAbstractClassDetectors for better performance.
     * And use the fromJson method without List<AbstractClassDetector> abstractClassDetectors
     *
     * @param json
     * @param clazz
     * @param abstractClassDetectors
     * @return
     * @param <T>
     */
    @Override
    @Deprecated
    public <T> T fromJson(String json, Class<T> clazz, List<AbstractClassDetector> abstractClassDetectors) {
        return JSON.parseObject(json, clazz, numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    @Override
    public <T> T fromJson(String json, TypeHolder<T> typeHolder) {
        return JSON.parseObject(json, typeHolder.getType(), numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

    @Override
    public <T> T fromJson(String json, TypeHolder<T> typeHolder, List<AbstractClassDetector> abstractClassDetectors) {
        return JSON.parseObject(json, typeHolder.getType(), numberParserConfig, Feature.OrderedField, /*Feature.UseNativeJavaObject, */Feature.DisableCircularReferenceDetect);
    }

}

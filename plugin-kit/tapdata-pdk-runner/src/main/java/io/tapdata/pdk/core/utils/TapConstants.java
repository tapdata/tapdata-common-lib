package io.tapdata.pdk.core.utils;

import com.alibaba.fastjson.parser.ParserConfig;
import io.tapdata.entity.schema.partition.type.TapPartitionType;
import io.tapdata.entity.schema.type.*;
import io.tapdata.entity.utils.JsonParser;
import io.tapdata.pdk.core.api.impl.AbstractResultDeserializer;

import java.util.Arrays;
import java.util.List;

public class TapConstants {
    public final static List<JsonParser.AbstractClassDetector> abstractClassDetectors = Arrays.asList(
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_DATETIME).deserializeClass(TapDateTime.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_ARRAY).deserializeClass(TapArray.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_BOOLEAN).deserializeClass(TapBoolean.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_MAP).deserializeClass(TapMap.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_YEAR).deserializeClass(TapYear.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_TIME).deserializeClass(TapTime.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_RAW).deserializeClass(TapRaw.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_NUMBER).deserializeClass(TapNumber.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_BINARY).deserializeClass(TapBinary.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_STRING).deserializeClass(TapString.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_DATE).deserializeClass(TapDate.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_MONEY).deserializeClass(TapMoney.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_XML).deserializeClass(TapXml.class),
            JsonParser.AbstractClassDetector.create().key("type").value(TapType.TYPE_JSON).deserializeClass(TapJson.class)
    );

    public final static ParserConfig tapdataParserConfig = new ParserConfig()/* {
        @Override
        public ObjectDeserializer getDeserializer(Type type) {
            if (type == TapType.class) {
                return new AbstractResultDeserializer(abstractClassDetectors);
            }
            return super.getDeserializer(type);
        }
    }*/;
    static {
        tapdataParserConfig.putDeserializer(TapType.class, new AbstractResultDeserializer(abstractClassDetectors));
        tapdataParserConfig.putDeserializer(TapPartitionType.class, new TapDeserializerBase<TapPartitionType>(TapPartitionType.KEY_NAME) {
            @Override
            public Class<? extends TapPartitionType> getByType(String typeObj) {
                return TapPartitionType.getTapPartitionTypeClass(typeObj);
            }
        });
    }
}

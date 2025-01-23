package io.tapdata.entity.codec;

import io.tapdata.entity.schema.value.*;
import io.tapdata.entity.utils.InstanceFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TapDefaultCodecs {
    static TapDefaultCodecs instance = new TapDefaultCodecs();

    Map<Class<?>, ToTapValueCodec<?>> classToTapValueCodecIgnoreMap = new ConcurrentHashMap<>();

    Map<String, ToTapValueCodec<?>> classToTapValueCodecMap = new ConcurrentHashMap<>();
    Map<String, ToTapValueCodec<?>> supportedClassToTapValueCodecMap = new ConcurrentHashMap<>();
    Map<String, FromTapValueCodec<?>> classFromTapValueCodecMap = new ConcurrentHashMap<>();

    public static final String TAP_TIME_VALUE = "TapTimeValue";
    public static final String TAP_MAP_VALUE = "TapMapValue";
    public static final String TAP_DATE_VALUE = "TapDateValue";
    public static final String TAP_ARRAY_VALUE = "TapArrayValue";
    public static final String TAP_YEAR_VALUE = "TapYearValue";
    public static final String TAP_NUMBER_VALUE = "TapNumberValue";
    public static final String TAP_BOOLEAN_VALUE = "TapBooleanValue";
    public static final String TAP_DATE_TIME_VALUE = "TapDateTimeValue";
    public static final String TAP_BINARY_VALUE = "TapBinaryValue";
    public static final String TAP_RAW_VALUE = "TapRawValue";
    public static final String TAP_STRING_VALUE = "TapStringValue";
    public static final String TAP_MONEY_VALUE = "TapMoneyValue";

    public TapDefaultCodecs() {
//        classToTapValueCodecIgnoreMap.put(byte[].class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BINARY_VALUE));
//        classToTapValueCodecIgnoreMap.put(boolean.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
//        classToTapValueCodecIgnoreMap.put(Boolean.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
//        classToTapValueCodecIgnoreMap.put(double.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Double.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(float.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Float.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(long.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Long.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(int.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Integer.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(short.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Short.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(byte.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(Byte.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecIgnoreMap.put(String.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_STRING_VALUE));

        supportedClassToTapValueCodecMap.put(byte[].class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_BINARY_VALUE));
        supportedClassToTapValueCodecMap.put(boolean.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
        supportedClassToTapValueCodecMap.put(Boolean.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
        supportedClassToTapValueCodecMap.put(double.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Double.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(float.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Float.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(long.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Long.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(int.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Integer.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(short.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Short.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(byte.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(Byte.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
        supportedClassToTapValueCodecMap.put(String.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_STRING_VALUE));
        supportedClassToTapValueCodecMap.put(Date.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_DATE_TIME_VALUE));
        supportedClassToTapValueCodecMap.put(DateTime.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_DATE_TIME_VALUE));
        supportedClassToTapValueCodecMap.put(double.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_MONEY_VALUE));
        supportedClassToTapValueCodecMap.put(Double.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_MONEY_VALUE));

//        classToTapValueCodecMap.put(byte[].class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BINARY_VALUE));
//        classToTapValueCodecMap.put(boolean.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
//        classToTapValueCodecMap.put(Boolean.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_BOOLEAN_VALUE));
//        classToTapValueCodecMap.put(double.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Double.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(float.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Float.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(long.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Long.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(int.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Integer.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(short.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Short.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(byte.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(Byte.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE));
//        classToTapValueCodecMap.put(String.class, InstanceFactory.instance(ToTapValueCodec.class, TAP_STRING_VALUE));

        classToTapValueCodecMap.put(Date.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_DATE_TIME_VALUE));
        classToTapValueCodecMap.put(DateTime.class.getName(), InstanceFactory.instance(ToTapValueCodec.class, TAP_DATE_TIME_VALUE));
        classFromTapValueCodecMap.put(TapArrayValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_ARRAY_VALUE));
        classFromTapValueCodecMap.put(TapBinaryValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_BINARY_VALUE));
        classFromTapValueCodecMap.put(TapBooleanValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_BOOLEAN_VALUE));
        classFromTapValueCodecMap.put(TapDateValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_DATE_VALUE));
        classFromTapValueCodecMap.put(TapDateTimeValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_DATE_TIME_VALUE));
        classFromTapValueCodecMap.put(TapMapValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_MAP_VALUE));
        classFromTapValueCodecMap.put(TapNumberValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_NUMBER_VALUE));
        classFromTapValueCodecMap.put(TapRawValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_RAW_VALUE));
        classFromTapValueCodecMap.put(TapStringValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_STRING_VALUE));
        classFromTapValueCodecMap.put(TapTimeValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_TIME_VALUE));
        classFromTapValueCodecMap.put(TapYearValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_YEAR_VALUE));
        classFromTapValueCodecMap.put(TapMoneyValue.class.getName(), InstanceFactory.instance(FromTapValueCodec.class, TAP_MONEY_VALUE));
    }

    ToTapValueCodec<?> getToTapValueCodec(Class<?> clazz) {
        ToTapValueCodec<?> codec = classToTapValueCodecMap.get(clazz.getName());
        if(codec != null)
            return codec;
//        if(classToTapValueCodecIgnoreMap.containsKey(clazz)) {
//            return null;
//        }
        if(Collection.class.isAssignableFrom(clazz)) {
            return InstanceFactory.instance(ToTapValueCodec.class, TAP_ARRAY_VALUE);
        } else if(Map.class.isAssignableFrom(clazz)) {
            return InstanceFactory.instance(ToTapValueCodec.class, TAP_MAP_VALUE);
        } /*else if(Number.class.isAssignableFrom(clazz)) {
            return InstanceFactory.instance(ToTapValueCodec.class, TAP_NUMBER_VALUE);
        }*/
//        return InstanceFactory.instance(ToTapValueCodec.class, TAP_RAW_VALUE);
        return null;
    }

    ToTapValueCodec<?> isRawCodec(Class<?> clazz) {
        if(!supportedClassToTapValueCodecMap.containsKey(clazz.getName())) {
            if(!Collection.class.isAssignableFrom(clazz) &&
                    !Map.class.isAssignableFrom(clazz) &&
                    !Number.class.isAssignableFrom(clazz)
            ) {
                return InstanceFactory.instance(ToTapValueCodec.class, TAP_RAW_VALUE);
            }
        }
        return null;
    }

    FromTapValueCodec<?> getFromTapValueCodec(Class<? extends TapValue<?, ?>> clazz) {
        return classFromTapValueCodecMap.get(clazz.getName());
    }

    public static void main(String[] args) {
        System.out.println("bool " + Number.class.isAssignableFrom(BigDecimal.class));
    }
}

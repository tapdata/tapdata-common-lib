package io.tapdata.pdk.core.api.impl.serialize;

import com.alibaba.fastjson.JSON;
import io.tapdata.entity.schema.value.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2025/11/20 12:11 Create
 */
class ObjectSerializableImplV2Test {

    ObjectSerializableImplV2 ins;
    Instant now;
    String zhCN;
    List<String> outOfOrderList;
    LinkedHashMap<String, Object> outOfOrderMap;

    @BeforeEach
    void setUp() {
        this.ins = new ObjectSerializableImplV2();
        this.now = Instant.now();
        this.zhCN = "abc中文123";
        this.outOfOrderList = Arrays.asList("3", "1", "a", "4", "A", "2");
        this.outOfOrderMap = new LinkedHashMap<>(Map.of(
            "3", 1,
            "1", "2",
            "a", 3,
            "4", 4,
            "A", 5,
            "2", 6
        ));
    }

    <T> void test(T v) {
        test(v, Assertions::assertEquals);
    }

    <T extends Comparable<T>> void testOfComparable(T v) {
        test(v, (from, to) -> {
            assertNotNull(to);
            assertEquals(from.getClass().getName(), to.getClass().getName());
            assertEquals(0, from.compareTo((T) to), String.format("from %s\n, to: %s\n", JSON.toJSONString(from), JSON.toJSONString(to)));
        });
    }

    <T> void testOfJson(T v) {
        test(v, (from, to) -> {
            String fromJsonStr = JSON.toJSONString(from);
            String toJsonStr = JSON.toJSONString(to);
            assertEquals(fromJsonStr, toJsonStr);
        });
    }

    <T> void test(T v, BiConsumer<T, Object> compare) {
        byte[] encodeBytes = ins.fromObject(v);
        assertNotNull(encodeBytes);
        Object decodeObj = ins.toObject(encodeBytes);
        assertNotNull(decodeObj);
        compare.accept(v, decodeObj);
    }

    @Test
    void test_null() {
        assertNull(ins.fromObject(null));
        assertNull(ins.toObject(null));
    }

    // ---------- 基础类型 ----------
    @Test void test_boolean() {test(false);}
    @Test void test_Boolean() {test(Boolean.TRUE);}
    @Test void test_short() {test((short) 1);}
    @Test void test_Short() {test(Short.valueOf("1"));}
    @Test void test_int() {test(1);}
    @Test void test_long() {test(1L);}
    @Test void test_float() {test(1.1f);}
    @Test void test_double() {test(1.1d);}
    @Test void test_BigInteger() {test(BigInteger.valueOf(1));}
    @Test void test_BigDecimal() {test(BigDecimal.valueOf(1));}
    @Test void test_byte() {test((byte) 'A');}
    @Test void test_bytes() {testOfJson(zhCN.getBytes());}
    @Test void test_String() {test(zhCN);}

    // ---------- 时间 ----------
    @Test void test_Instant() {testOfComparable(now);}
    @Test void test_java_sql_Date() {testOfComparable(java.sql.Date.from(now));}
    @Test void test_java_sql_Time() {testOfComparable(java.sql.Time.from(now));}
    @Test void test_java_sql_Timestamp() {testOfJson(java.sql.Timestamp.from(now));}
    @Test void test_java_util_Date() {testOfComparable(java.util.Date.from(now));}

    // ---------- 集合 ----------
    @Test void test_List() {test(outOfOrderList);}
    @Test void test_HashSet() {test(new HashSet<>(outOfOrderList));}
    @Test void test_LinkedHashSet() {test(new LinkedHashSet<>(outOfOrderList));}
    @Test void test_HashMap() {test(new HashMap<>(outOfOrderMap));}
    @Test void test_LinkedHashMap() {test(outOfOrderMap);}

    // ---------- TapValue ----------
    @Test void test_TapArrayValue() {testOfJson(new TapArrayValue(new ArrayList<>(outOfOrderList)));}
    @Test void test_TapBinaryValue() {testOfJson(new TapBinaryValue(zhCN.getBytes()));}
    @Test void test_TapBooleanValue() {testOfJson(new TapBooleanValue(true));}
    @Test void test_TapDateTimeValue() {testOfJson(new TapDateTimeValue(DateTime.withDateStr("2025-11-20")));}
    @Test void test_TapDateValue() {testOfJson(new TapDateValue(DateTime.withDateStr("2025-11-20")));}
    @Test void test_TapInputStreamValue() {
        // IO 流暂不支持序列化操作
        TapInputStreamValue v = new TapInputStreamValue(new ByteArrayInputStream(zhCN.getBytes()));
        byte[] encodeBytes = ins.fromObject(v);
        assertNotNull(encodeBytes);
        Object decodeObj = ins.toObject(encodeBytes);
        assertNull(decodeObj);
    }
    @Test void test_TapJsonValue() {testOfJson(new TapJsonValue("{\"id\":1,\"title\":\"" + zhCN + "\"}"));}
    @Test void test_TapMapValue() {testOfJson(new TapMapValue(outOfOrderMap));}
    @Test void test_TapMoneyValue() {testOfJson(new TapMoneyValue(1.1));}
    @Test void test_TapNumberValue() {testOfJson(new TapNumberValue(1.1));}
    @Test void test_TapRawValue() {testOfJson(new TapRawValue(zhCN.getBytes()));}
    @Test void test_TapStringValue() {testOfJson(new TapStringValue(zhCN));}
    @Test void test_TapTimeValue() {testOfJson(new TapTimeValue(DateTime.withTimeStr("13:28:01")));}
    @Test void test_TapXmlValue() {testOfJson(new TapXmlValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><title>" + zhCN + "</title></root>"));}
    @Test void test_TapYearValue() {testOfJson(new TapYearValue(1));}

    // ---------- 其它 ----------
}

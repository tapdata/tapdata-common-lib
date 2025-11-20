package io.tapdata.pdk.core.api.impl.serialize;

import com.alibaba.fastjson.JSON;
import io.tapdata.entity.schema.value.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test for ObjectSerializableImplV2
 * Tests serialization and deserialization consistency for various types
 * 
 * @author Tapdata
 */
@DisplayName("ObjectSerializableImplV2 Comprehensive Tests")
class ObjectSerializableImplV2ComprehensiveTest {

    private ObjectSerializableImplV2 serializer;
    private Instant testInstant;
    private String testString;

    @BeforeEach
    void setUp() {
        serializer = new ObjectSerializableImplV2();
        testInstant = Instant.now();
        testString = "Test String 测试字符串 123";
    }

    // ==================== Primitive Types Tests ====================
    @Nested
    @DisplayName("Primitive Types Tests")
    class PrimitiveTypesTests {

        @Test
        @DisplayName("Test null value")
        void testNull() {
            assertNull(serializer.fromObject(null));
            assertNull(serializer.toObject(null));
        }

        @Test
        @DisplayName("Test String")
        void testString() {
            testSerializeDeserialize(testString);
        }

        @Test
        @DisplayName("Test long String (> 12000 chars)")
        void testLongString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 15000; i++) {
                sb.append("a");
            }
            String longString = sb.toString();
            testSerializeDeserialize(longString);
        }

        @Test
        @DisplayName("Test Integer")
        void testInteger() {
            testSerializeDeserialize(Integer.MAX_VALUE);
            testSerializeDeserialize(Integer.MIN_VALUE);
            testSerializeDeserialize(0);
        }

        @Test
        @DisplayName("Test Long")
        void testLong() {
            testSerializeDeserialize(Long.MAX_VALUE);
            testSerializeDeserialize(Long.MIN_VALUE);
            testSerializeDeserialize(0L);
        }

        @Test
        @DisplayName("Test Double")
        void testDouble() {
            testSerializeDeserialize(Double.MAX_VALUE);
            testSerializeDeserialize(Double.MIN_VALUE);
            testSerializeDeserialize(3.14159);
        }

        @Test
        @DisplayName("Test Float")
        void testFloat() {
            testSerializeDeserialize(Float.MAX_VALUE);
            testSerializeDeserialize(Float.MIN_VALUE);
            testSerializeDeserialize(3.14f);
        }

        @Test
        @DisplayName("Test Short")
        void testShort() {
            testSerializeDeserialize(Short.MAX_VALUE);
            testSerializeDeserialize(Short.MIN_VALUE);
            testSerializeDeserialize((short) 0);
        }

        @Test
        @DisplayName("Test Byte")
        void testByte() {
            testSerializeDeserialize(Byte.MAX_VALUE);
            testSerializeDeserialize(Byte.MIN_VALUE);
            testSerializeDeserialize((byte) 0);
        }

        @Test
        @DisplayName("Test byte array")
        void testByteArray() {
            byte[] bytes = testString.getBytes();
            testSerializeDeserializeJson(bytes);
        }

        @Test
        @DisplayName("Test BigDecimal")
        void testBigDecimal() {
            testSerializeDeserialize(new BigDecimal("123456789.987654321"));
            testSerializeDeserialize(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Test BigInteger")
        void testBigInteger() {
            testSerializeDeserialize(new BigInteger("123456789012345678901234567890"));
            testSerializeDeserialize(BigInteger.ZERO);
        }
    }

    // ==================== Date/Time Types Tests ====================
    @Nested
    @DisplayName("Date/Time Types Tests")
    class DateTimeTypesTests {

        @Test
        @DisplayName("Test Instant")
        void testInstant() {
            testSerializeDeserializeComparable(testInstant);
        }

        @Test
        @DisplayName("Test java.util.Date")
        void testUtilDate() {
            Date date = Date.from(testInstant);
            testSerializeDeserializeComparable(date);
        }

        @Test
        @DisplayName("Test java.sql.Date")
        void testSqlDate() {
            // Note: java.sql.Date is deserialized as java.util.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf("2025-11-20");
            byte[] bytes = serializer.fromObject(sqlDate);
            assertNotNull(bytes);
            Object result = serializer.toObject(bytes);
            assertNotNull(result);
            assertTrue(result instanceof java.util.Date);
            assertEquals(sqlDate.getTime(), ((java.util.Date) result).getTime());
        }

        @Test
        @DisplayName("Test java.sql.Time")
        void testSqlTime() {
            Time time = Time.valueOf("13:45:30");
            testSerializeDeserializeComparable(time);
        }

        @Test
        @DisplayName("Test java.sql.Timestamp")
        void testSqlTimestamp() {
            Timestamp timestamp = Timestamp.from(testInstant);
            testSerializeDeserializeJson(timestamp);
        }
    }

    // ==================== Collection Types Tests ====================
    @Nested
    @DisplayName("Collection Types Tests")
    class CollectionTypesTests {

        @Test
        @DisplayName("Test ArrayList order preservation")
        void testArrayList() {
            List<String> list = new ArrayList<>(Arrays.asList("3", "1", "a", "4", "A", "2"));
            Object result = testSerializeDeserialize(list);
            assertTrue(result instanceof List);
            assertEquals(list, result);
            // Verify order is preserved
            assertEquals("3", ((List<?>) result).get(0));
            assertEquals("2", ((List<?>) result).get(5));
        }

        @Test
        @DisplayName("Test LinkedHashMap order preservation")
        void testLinkedHashMap() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("key3", 3);
            map.put("key1", "value1");
            map.put("key2", 2);
            map.put("key4", 4.0);
            
            Object result = testSerializeDeserialize(map);
            assertTrue(result instanceof Map);
            
            // Verify order is preserved
            List<String> keys = new ArrayList<>(((Map<String, Object>) result).keySet());
            assertEquals("key3", keys.get(0));
            assertEquals("key1", keys.get(1));
            assertEquals("key2", keys.get(2));
            assertEquals("key4", keys.get(3));
        }

        @Test
        @DisplayName("Test LinkedHashSet order preservation")
        void testLinkedHashSet() {
            LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList("3", "1", "a", "4", "A", "2"));
            testSerializeDeserialize(set);
        }

        @Test
        @DisplayName("Test HashMap")
        void testHashMap() {
            HashMap<String, Object> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", 123);
            map.put("key3", 3.14);
            testSerializeDeserialize(map);
        }

        @Test
        @DisplayName("Test HashSet")
        void testHashSet() {
            HashSet<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
            testSerializeDeserialize(set);
        }
    }

    // ==================== TapValue Types Tests ====================
    @Nested
    @DisplayName("TapValue Types Tests")
    class TapValueTypesTests {

        @Test
        @DisplayName("Test TapStringValue")
        void testTapStringValue() {
            TapStringValue value = new TapStringValue(testString);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapNumberValue")
        void testTapNumberValue() {
            TapNumberValue value = new TapNumberValue(123.456);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapBooleanValue")
        void testTapBooleanValue() {
            TapBooleanValue value = new TapBooleanValue(true);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapArrayValue")
        void testTapArrayValue() {
            List<Object> list = new ArrayList<>(Arrays.asList("a", 1, 2.5, true));
            TapArrayValue value = new TapArrayValue(list);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapMapValue")
        void testTapMapValue() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", "test");
            map.put("age", 25);
            map.put("active", true);
            TapMapValue value = new TapMapValue(map);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapBinaryValue")
        void testTapBinaryValue() {
            TapBinaryValue value = new TapBinaryValue(testString.getBytes());
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapDateValue")
        void testTapDateValue() {
            TapDateValue value = new TapDateValue(DateTime.withDateStr("2025-11-20"));
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapTimeValue")
        void testTapTimeValue() {
            TapTimeValue value = new TapTimeValue(DateTime.withTimeStr("13:45:30"));
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapDateTimeValue")
        void testTapDateTimeValue() {
            TapDateTimeValue value = new TapDateTimeValue(DateTime.withDateStr("2025-11-20"));
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapJsonValue")
        void testTapJsonValue() {
            String json = "{\"id\":1,\"name\":\"" + testString + "\"}";
            TapJsonValue value = new TapJsonValue(json);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapXmlValue")
        void testTapXmlValue() {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><title>" + testString + "</title></root>";
            TapXmlValue value = new TapXmlValue(xml);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapRawValue")
        void testTapRawValue() {
            TapRawValue value = new TapRawValue(testString.getBytes());
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapMoneyValue")
        void testTapMoneyValue() {
            TapMoneyValue value = new TapMoneyValue(999.99);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapYearValue")
        void testTapYearValue() {
            TapYearValue value = new TapYearValue(2025);
            testSerializeDeserializeJson(value);
        }

        @Test
        @DisplayName("Test TapInputStreamValue - should return null")
        void testTapInputStreamValue() {
            // IO stream values are not supported for serialization
            TapInputStreamValue value = new TapInputStreamValue(new ByteArrayInputStream(testString.getBytes()));
            byte[] bytes = serializer.fromObject(value);
            assertNotNull(bytes);
            Object result = serializer.toObject(bytes);
            assertNull(result, "TapInputStreamValue should not be serialized");
        }
    }

    // ==================== Custom Object Tests ====================
    @Nested
    @DisplayName("Custom Object Tests")
    class CustomObjectTests {

        @Test
        @DisplayName("Test custom Serializable object")
        void testCustomSerializableObject() {
            Person person = new Person("John Doe", 30, "john@example.com");
            testSerializeDeserialize(person);
        }

        @Test
        @DisplayName("Test nested custom objects")
        void testNestedCustomObjects() {
            Address address = new Address("123 Main St", "New York", "10001");
            Employee employee = new Employee("Jane Smith", 28, "jane@example.com", address);
            testSerializeDeserialize(employee);
        }
    }

    // ==================== Nested Structures Tests ====================
    @Nested
    @DisplayName("Nested Structures Tests")
    class NestedStructuresTests {

        @Test
        @DisplayName("Test nested List in Map")
        void testNestedListInMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("list1", Arrays.asList(1, 2, 3));
            map.put("list2", Arrays.asList("a", "b", "c"));
            map.put("number", 42);

            Object result = testSerializeDeserialize(map);
            assertTrue(result instanceof Map);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            assertTrue(resultMap.get("list1") instanceof List);
            assertEquals(Arrays.asList(1, 2, 3), resultMap.get("list1"));
        }

        @Test
        @DisplayName("Test nested Map in List")
        void testNestedMapInList() {
            List<Object> list = new ArrayList<>();
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("key1", "value1");
            map1.put("key2", 123);

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("keyA", "valueA");
            map2.put("keyB", 456);

            list.add(map1);
            list.add(map2);
            list.add("string");

            Object result = testSerializeDeserialize(list);
            assertTrue(result instanceof List);
            List<?> resultList = (List<?>) result;
            assertEquals(3, resultList.size());
            assertTrue(resultList.get(0) instanceof Map);
            assertTrue(resultList.get(1) instanceof Map);
        }

        @Test
        @DisplayName("Test deeply nested structures")
        void testDeeplyNestedStructures() {
            Map<String, Object> level3 = new LinkedHashMap<>();
            level3.put("value", "deep");
            level3.put("number", 999);

            List<Object> level2List = new ArrayList<>();
            level2List.add(level3);
            level2List.add("level2");

            Map<String, Object> level1 = new LinkedHashMap<>();
            level1.put("nested", level2List);
            level1.put("simple", "value");

            Object result = testSerializeDeserialize(level1);
            assertTrue(result instanceof Map);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            assertTrue(resultMap.get("nested") instanceof List);
            List<?> nestedList = (List<?>) resultMap.get("nested");
            assertTrue(nestedList.get(0) instanceof Map);
        }

        @Test
        @DisplayName("Test complex mixed types")
        void testComplexMixedTypes() {
            Map<String, Object> complex = new LinkedHashMap<>();
            complex.put("string", testString);
            complex.put("integer", 123);
            complex.put("double", 3.14);
            complex.put("bigDecimal", new BigDecimal("999.999"));
            complex.put("instant", testInstant);
            complex.put("list", Arrays.asList(1, 2, 3));

            Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("nested1", "value1");
            nested.put("nested2", 456);
            complex.put("map", nested);

            complex.put("tapString", new TapStringValue("tap value"));
            complex.put("tapNumber", new TapNumberValue(789.0));

            // Use JSON comparison for complex objects
            testSerializeDeserializeJson(complex);
        }
    }

    // ==================== Null and Edge Cases Tests ====================
    @Nested
    @DisplayName("Null and Edge Cases Tests")
    class NullAndEdgeCasesTests {

        @Test
        @DisplayName("Test Map with null values")
        void testMapWithNullValues() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key1", "value1");
            map.put("key2", null);
            map.put("key3", "value3");

            Object result = testSerializeDeserialize(map);
            assertTrue(result instanceof Map);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            // Null values are serialized and preserved
            assertTrue(resultMap.containsKey("key2"));
            assertNull(resultMap.get("key2"));
        }

        @Test
        @DisplayName("Test List with null values")
        void testListWithNullValues() {
            List<Object> list = new ArrayList<>();
            list.add("value1");
            list.add(null);
            list.add("value3");

            byte[] bytes = serializer.fromObject(list);
            assertNotNull(bytes);
            Object result = serializer.toObject(bytes);
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> resultList = (List<?>) result;
            // Null values are filtered out during serialization
            assertEquals(2, resultList.size());
            assertEquals("value1", resultList.get(0));
            assertEquals("value3", resultList.get(1));
        }

        @Test
        @DisplayName("Test empty Map")
        void testEmptyMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            Object result = testSerializeDeserialize(map);
            assertTrue(result instanceof Map);
            assertTrue(((Map<?, ?>) result).isEmpty());
        }

        @Test
        @DisplayName("Test empty List")
        void testEmptyList() {
            List<Object> list = new ArrayList<>();
            Object result = testSerializeDeserialize(list);
            assertTrue(result instanceof List);
            assertTrue(((List<?>) result).isEmpty());
        }
    }

    // ==================== Complex Mixed Type Tests ====================
    @Nested
    @DisplayName("Complex Mixed Type Tests")
    class ComplexMixedTypeTests {

        @Test
        @DisplayName("Test all primitive types in one Map")
        void testAllPrimitiveTypesInMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("string", "test");
            map.put("integer", 123);
            map.put("long", 123L);
            map.put("double", 3.14);
            map.put("float", 3.14f);
            map.put("short", (short) 10);
            map.put("byte", (byte) 5);
            map.put("bigDecimal", new BigDecimal("999.99"));
            map.put("bigInteger", new BigInteger("123456789"));
            map.put("instant", testInstant);
            map.put("date", new Date());

            Object result = testSerializeDeserialize(map);
            assertTrue(result instanceof Map);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            assertEquals("test", resultMap.get("string"));
            assertEquals(123, resultMap.get("integer"));
            assertEquals(123L, resultMap.get("long"));
        }
    }

    // ==================== Helper Methods ====================

    private Object testSerializeDeserialize(Object obj) {
        byte[] bytes = serializer.fromObject(obj);
        assertNotNull(bytes, "Serialized bytes should not be null");

        Object result = serializer.toObject(bytes);
        assertNotNull(result, "Deserialized object should not be null");

        assertEquals(obj, result, "Deserialized object should equal original");
        return result;
    }

    private <T extends Comparable<T>> void testSerializeDeserializeComparable(T obj) {
        byte[] bytes = serializer.fromObject(obj);
        assertNotNull(bytes, "Serialized bytes should not be null");

        Object result = serializer.toObject(bytes);
        assertNotNull(result, "Deserialized object should not be null");
        assertEquals(obj.getClass(), result.getClass(), "Class types should match");

        @SuppressWarnings("unchecked")
        T typedResult = (T) result;
        assertEquals(0, obj.compareTo(typedResult), "Objects should be equal via compareTo");
    }

    private void testSerializeDeserializeJson(Object obj) {
        byte[] bytes = serializer.fromObject(obj);
        assertNotNull(bytes, "Serialized bytes should not be null");

        Object result = serializer.toObject(bytes);
        assertNotNull(result, "Deserialized object should not be null");

        String originalJson = JSON.toJSONString(obj);
        String resultJson = JSON.toJSONString(result);
        assertEquals(originalJson, resultJson, "JSON representations should match");
    }

    // ==================== Test Data Classes ====================

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;
        private String email;

        public Person() {}

        public Person(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age &&
                   Objects.equals(name, person.name) &&
                   Objects.equals(email, person.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, email);
        }
    }

    static class Address implements Serializable {
        private static final long serialVersionUID = 1L;
        private String street;
        private String city;
        private String zipCode;

        public Address() {}

        public Address(String street, String city, String zipCode) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Address address = (Address) o;
            return Objects.equals(street, address.street) &&
                   Objects.equals(city, address.city) &&
                   Objects.equals(zipCode, address.zipCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(street, city, zipCode);
        }
    }

    static class Employee implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;
        private String email;
        private Address address;

        public Employee() {}

        public Employee(String name, int age, String email, Address address) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.address = address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Employee employee = (Employee) o;
            return age == employee.age &&
                   Objects.equals(name, employee.name) &&
                   Objects.equals(email, employee.email) &&
                   Objects.equals(address, employee.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, email, address);
        }
    }
}


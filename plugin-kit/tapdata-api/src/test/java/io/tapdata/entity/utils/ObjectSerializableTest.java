package io.tapdata.entity.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2025/11/20 12:02 Create
 */
class ObjectSerializableTest {

    @Nested
    class ToObjectOptionsTest {

        ObjectSerializable.ToObjectOptions options;
        Class<ObjectSerializableTest> skipClass;
        Class<ToObjectOptionsTest> unskipClass;

        @BeforeEach
        void setUp() {
            options = new ObjectSerializable.ToObjectOptions();
            skipClass = ObjectSerializableTest.class;
            unskipClass = ToObjectOptionsTest.class;
        }

        @Test
        void testDefault() {
            assertNull(options.getClassLoader());
            assertFalse(options.isSkipSerialVersionUID());
            assertNull(options.getSkipSerialVersionUIDClassNames());

            assertFalse(options.isSkipSerialVersionUID(skipClass));
            assertFalse(options.isSkipSerialVersionUID(unskipClass));
        }

        @Test
        void test_skipAllSerialVersionUID() {
            options.setSkipSerialVersionUID(true);

            assertTrue(options.isSkipSerialVersionUID(skipClass));
            assertTrue(options.isSkipSerialVersionUID(unskipClass));
        }

        @Test
        void test_isSkipSerialVersionUID() {
            options.skipSerialVersionUIDClassNames(skipClass);

            assertTrue(options.isSkipSerialVersionUID(skipClass));
            assertFalse(options.isSkipSerialVersionUID(unskipClass));
        }
    }
}

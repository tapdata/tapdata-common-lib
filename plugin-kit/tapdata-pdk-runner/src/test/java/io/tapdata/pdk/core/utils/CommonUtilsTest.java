package io.tapdata.pdk.core.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/**
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2023/12/25 14:11 Create
 */
class CommonUtilsTest {

    @Test
    void testDateString() {
        String expectedValue = "1970-01-01 08:00:00,000";
        Instant testInstant = Instant.parse("1970-01-01T00:00:00Z");
        Date testDate = Date.from(testInstant);

        System.out.println(TimeZone.getDefault().getRawOffset());

        assertEquals(expectedValue, CommonUtils.dateString(testDate));
    }

    @Nested
    class TestGetProperty {
        final String testKeyExists = "test_key_exists";
        final String testExpectedValueExists = "test_value";

        @BeforeEach
        void beforeAll() {
            CommonUtils.setProperty(testKeyExists, testExpectedValueExists);
        }

        @Test
        void testPropertyExists() {
            try (MockedStatic<CommonUtils> ignore = mockStatic(CommonUtils.class, CALLS_REAL_METHODS)) {
                assertEquals(testExpectedValueExists, CommonUtils.getProperty(testKeyExists));
            }
        }

        @Test
        void testUseEnv() {
            String key = "test_key_env";
            String expectedUseDefault = "test_value_env";
            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class, CALLS_REAL_METHODS)) {
                commonUtilsMockedStatic.when(() -> CommonUtils.getenv(key)).thenReturn(expectedUseDefault);
                assertEquals(expectedUseDefault, CommonUtils.getProperty(key));
            }
        }

        @Test
        void testDefault() {
            // use default
            String key = "test_key_default";
            String expectedUseDefault = "test_value_default";
            assertEquals(expectedUseDefault, CommonUtils.getProperty(key, expectedUseDefault));

            // use property
            assertEquals(testExpectedValueExists, CommonUtils.getProperty(testKeyExists, null));
        }

        @Test
        void testBool() {
            String testKey = "test_bool_key";
            boolean expectedTrue = true;
            boolean expectedFalse = false;


            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class, CALLS_REAL_METHODS)) {
                // use default
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedTrue));

                commonUtilsMockedStatic.when(() -> CommonUtils.getProperty(testKey)).thenReturn(String.valueOf(expectedTrue), "error"
                    , "y", "yes", "t", "true", "1"
                    , "n", "no", "f", "false", "0");
                // use property
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedTrue));
                // error use default
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedTrue));
                // true values
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedFalse)); // y
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedFalse)); // yes
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedFalse)); // t
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedFalse)); // true
                assertEquals(expectedTrue, CommonUtils.getPropertyBool(testKey, expectedFalse)); // 1
                // false values
                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // n
                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // no
                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // f
                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // false
                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // 0

                assertEquals(expectedFalse, CommonUtils.getPropertyBool(testKey, expectedTrue)); // 0
            }
        }

        @Test
        void testInt() {
            String testKey = "test_int_key";
            int expectedValue = 1;
            int defValue = 0;

            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class, CALLS_REAL_METHODS)) {
                // use default
                assertEquals(expectedValue, CommonUtils.getPropertyInt(testKey, expectedValue));

                commonUtilsMockedStatic.when(() -> CommonUtils.getProperty(testKey)).thenReturn(String.valueOf(expectedValue), "error");
                // use property
                assertEquals(expectedValue, CommonUtils.getPropertyInt(testKey, defValue));
                // error use default
                assertEquals(defValue, CommonUtils.getPropertyInt(testKey, defValue));
            }
        }

        @Test
        void testLong() {
            String testKey = "test_long_key";
            int expectedValue = 1;
            int defValue = 0;

            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class, CALLS_REAL_METHODS)) {
                // use default
                assertEquals(expectedValue, CommonUtils.getPropertyLong(testKey, expectedValue));

                commonUtilsMockedStatic.when(() -> CommonUtils.getProperty(testKey)).thenReturn(String.valueOf(expectedValue), "error");
                // use property
                assertEquals(expectedValue, CommonUtils.getPropertyLong(testKey, defValue));
                // error use default
                assertEquals(defValue, CommonUtils.getPropertyLong(testKey, defValue));
            }
        }
    }

}

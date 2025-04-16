package io.tapdata.entity.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2025/4/16 09:17 Create
 */
class TapFieldTest {

    TapField tapField;

    @BeforeEach
    void setUp() {
        tapField = new TapField();
    }

    @Nested
    class originalFieldNameTest {
        @Test
        void testOriginalFieldName_AttrExists() {
            Assertions.assertDoesNotThrow(() -> {
                Field declaredField = TapField.class.getDeclaredField(TapField.FIELD_ORIGINAL_FIELD_NAME);
                Assertions.assertNotNull(declaredField);
            });
        }

        @Test
        void testOriginalFieldName_SetterAndGetter() {
            String expect = "test";
            tapField.setOriginalFieldName(expect);
            Assertions.assertEquals(expect, tapField.getOriginalFieldName());
        }
    }

    @Test
    void testClone() {
        TapField clone = tapField.clone();
        Assertions.assertEquals(tapField.getName(), clone.getName());
        Assertions.assertEquals(tapField.getOriginalFieldName(), clone.getOriginalFieldName());
        Assertions.assertEquals(tapField.getDataType(), clone.getDataType());
        Assertions.assertEquals(tapField.getPureDataType(), clone.getPureDataType());
        Assertions.assertEquals(tapField.getLength(), clone.getLength());
        Assertions.assertEquals(tapField.getScale(), clone.getScale());
        Assertions.assertEquals(tapField.getPrecision(), clone.getPrecision());
        Assertions.assertEquals(tapField.getPartitionKeyPos(), clone.getPartitionKeyPos());
        Assertions.assertEquals(tapField.getPos(), clone.getPos());
        Assertions.assertEquals(tapField.getPrimaryKeyPos(), clone.getPrimaryKeyPos());
        Assertions.assertEquals(tapField.getAutoInc(), clone.getAutoInc());
        Assertions.assertEquals(tapField.getAutoIncStartValue(), clone.getAutoIncStartValue());
        Assertions.assertEquals(tapField.getAutoIncrementValue(), clone.getAutoIncrementValue());
        Assertions.assertEquals(tapField.getAutoIncCacheValue(), clone.getAutoIncCacheValue());
        Assertions.assertEquals(tapField.getSequenceName(), clone.getSequenceName());
        Assertions.assertEquals(tapField.getCheck(), clone.getCheck());
        Assertions.assertEquals(tapField.getComment(), clone.getComment());
        Assertions.assertEquals(tapField.getConstraint(), clone.getConstraint());
        Assertions.assertEquals(tapField.getDefaultValue(), clone.getDefaultValue());
        Assertions.assertEquals(tapField.getDefaultFunction(), clone.getDefaultFunction());
        Assertions.assertEquals(tapField.getPartitionKey(), clone.getPartitionKey());
        Assertions.assertEquals(tapField.getPrimaryKey(), clone.getPrimaryKey());
        Assertions.assertEquals(tapField.getTapType(), clone.getTapType());
        Assertions.assertEquals(tapField.getCreateSource(), clone.getCreateSource());
    }
}

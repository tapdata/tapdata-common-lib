package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.impl.ToTapNumberCodec;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.value.TapNumberValue;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class TapNumberFilterCompatibilityTest {
    @Test void bothFiltersPreserveExactPayloadAndPassItToChecks() {
        for (boolean enforced : new boolean[]{false, true}) {
            for (Number number : new Number[]{5, 9007199254740993L, new BigDecimal("9007199254740993.00100")}) {
                TapCodecsRegistry registry = new TapCodecsRegistry();
                registry.registerToTapValue(number.getClass(), new ToTapNumberCodec());
                TapCodecsFilterManager manager = enforced ? new TapCodecsFilterManagerSchemaEnforced(registry)
                        : TapCodecsFilterManager.create(registry);
                Map<String, TapField> fields = Collections.singletonMap("n", new TapField("n", "NUMBER").tapType(new TapNumber()));
                TapTable table = new TapTable("numbers");
                table.setNameFieldMap(new LinkedHashMap<>(fields));
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("n", number);
                if (enforced) ((TapCodecsFilterManagerSchemaEnforced) manager).transformToTapValueMap(record, table);
                else manager.transformToTapValueMap(record, fields);
                Number expected = number instanceof Integer ? Double.valueOf(number.doubleValue()) : number;
                assertEquals(expected, ((TapNumberValue) record.get("n")).getNumberValue());
                AtomicReference<Object> checked = new AtomicReference<>();
                ToTapValueCheck check = (key, value) -> { checked.set(value); return true; };
                record.put("n", number);
                if (enforced) ((TapCodecsFilterManagerSchemaEnforced) manager).transformToTapValueMap(record, table, check);
                else manager.transformToTapValueMap(record, fields, check);
                assertEquals(expected, checked.get());
            }
        }
    }
}

package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.schema.type.TapCoefficientFloat;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.schema.value.TapFloatValue;
import io.tapdata.entity.schema.value.TapRawValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static io.tapdata.entity.simplify.TapSimplify.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author samuel
 * @Description
 * @create 2024-06-24 17:07
 **/
@DisplayName("Class TapCodecsFilterManager Test")
class TapCodecsFilterManagerTest {
	@Nested
	@DisplayName("Method fieldName test")
	class fieldNameTest {
		@Test
		@DisplayName("input key: \"aaa.#1.bbb\", expect: \"aaa.bbb\"")
		void test1() {
			assertEquals("aaa.bbb", TapCodecsFilterManager.fieldName("aaa.#1.bbb"));
		}

		@Test
		@DisplayName("input key: \"aaa.#1.#2bbb\", expect: \"aaa.#2bbb\"")
		void test2() {
			assertEquals("aaa.#2bbb", TapCodecsFilterManager.fieldName("aaa.#1.#2bbb"));
		}

		@Test
		@DisplayName("input key: null, expect: null")
		void test3() {
			assertNull(TapCodecsFilterManager.fieldName(null));
		}

		@Test
		@DisplayName("input key: empty string, expect: same")
		void test4() {
			String key = "";
			assertSame(key, TapCodecsFilterManager.fieldName(key));
		}

		@Test
		@DisplayName("input key: \"a.b.c\", expect: same")
		void test5() {
			String key = "a.b.c";
			assertSame(key, TapCodecsFilterManager.fieldName(key));
		}
	}

	@Test
	void shouldUseConcreteCodecForAllNumericProtocolTypes() {
		ToTapValueCodec<?> codec = mock(ToTapValueCodec.class);
		TapFloat type = new TapFloat() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return codec;
			}
		};

		assertSame(codec, new ExposedFilterManager().valueCodec(type));
	}

	@Test
	@DisplayName("TYPE_NUMBER only dispatches concrete codecs for TapFloat/TapDouble, plain numbers pass through")
	void shouldOnlyDispatchConcreteCodecForFloatAndDouble() {
		ToTapValueCodec<?> codec = mock(ToTapValueCodec.class);
		ExposedFilterManager filterManager = new ExposedFilterManager();

		TapFloat tapFloat = new TapFloat() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return codec;
			}
		};
		TapDouble tapDouble = new TapDouble() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return codec;
			}
		};

		assertSame(codec, filterManager.valueCodec(tapFloat), "TapFloat should dispatch to its concrete codec");
		assertSame(codec, filterManager.valueCodec(tapDouble), "TapDouble should dispatch to its concrete codec");
		assertNull(filterManager.valueCodec(new TapNumber()), "plain TapNumber must pass through with no codec");
		assertNull(filterManager.valueCodec(new TapCoefficientFloat()), "TapCoefficientFloat must pass through with no codec");
	}

	@Test
	@DisplayName("plain numeric fields pass through transformToTapValueMap unchanged (no ToTapNumberCodec wrap)")
	void plainNumberFieldsPassThroughTransformToTapValueMap() {
		TapCodecsFilterManager filterManager = TapCodecsFilterManager.create(new TapCodecsRegistry());
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("longField", 1234567890123L);
		record.put("bigDecimalField", new BigDecimal("123456789.0123456789"));
		record.put("intField", 42);

		Map<String, TapField> nameFieldMap = new LinkedHashMap<>();
		nameFieldMap.put("longField", field("longField", "number(64)").tapType(new TapNumber()));
		nameFieldMap.put("bigDecimalField", field("bigDecimalField", "number").tapType(new TapNumber()));
		nameFieldMap.put("intField", field("intField", "number(32)").tapType(new TapNumber()));

		filterManager.transformToTapValueMap(record, nameFieldMap);

		assertEquals(1234567890123L, record.get("longField"), "Long must keep its value and type");
		assertEquals(new BigDecimal("123456789.0123456789"), record.get("bigDecimalField"), "BigDecimal must keep its precision");
		assertEquals(42, record.get("intField"));
		assertFalse(record.get("longField") instanceof TapValue, "plain number must not be wrapped into TapValue");
		assertFalse(record.get("bigDecimalField") instanceof TapValue, "BigDecimal must not be wrapped into TapValue");
	}

	@Test
	@DisplayName("compatibility mode keeps TapValue wrappers for floating-point schema types")
	void compatibilityModeKeepsTapValueForFloatingPointFields() {
		TapCodecsFilterManager filterManager = TapCodecsFilterManager.create(new TapCodecsRegistry());
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("floatField", 1.234567890123d);
		record.put("doubleField", 1.234567890123d);

		Map<String, TapField> nameFieldMap = new LinkedHashMap<>();
		nameFieldMap.put("floatField", field("floatField", "float").tapType(new TapFloat() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return new io.tapdata.entity.codec.impl.ToTapFloatCodec();
			}
		}));
		nameFieldMap.put("doubleField", field("doubleField", "double").tapType(new TapDouble() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return new io.tapdata.entity.codec.impl.ToTapDoubleCodec();
			}
		}));

		filterManager.transformToTapValueMap(record, nameFieldMap);

		assertInstanceOf(TapFloatValue.class, record.get("floatField"));
		assertTrue(record.get("doubleField") instanceof TapValue);
	}

	@Test
	@DisplayName("raw mode emits concrete floating-point values and leaves plain numbers untouched")
	void rawModeAvoidsTapValueAllocationForFloatingPointFields() {
		TapCodecsFilterManager filterManager = TapCodecsFilterManager.create(new TapCodecsRegistry());
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("floatField", 1.234567890123d);
		record.put("doubleField", 1.234567890123d);
		record.put("longField", 42L);

		Map<String, TapField> nameFieldMap = new LinkedHashMap<>();
		nameFieldMap.put("floatField", field("floatField", "float").tapType(new TapFloat()));
		nameFieldMap.put("doubleField", field("doubleField", "double").tapType(new TapDouble()));
		nameFieldMap.put("longField", field("longField", "number").tapType(new TapNumber()));

		filterManager.transformToTapValueMap(FloatingPointTransformMode.RAW, record, nameFieldMap);

		assertInstanceOf(Double.class, record.get("floatField"));
		assertEquals((double) (float) 1.234567890123d, record.get("floatField"));
		assertInstanceOf(Double.class, record.get("doubleField"));
		assertEquals(1.234567890123d, record.get("doubleField"));
		assertEquals(42L, record.get("longField"));
		assertFalse(record.get("floatField") instanceof TapValue);
		assertFalse(record.get("doubleField") instanceof TapValue);
	}

	@Test
	@DisplayName("raw mode gives custom codecs and origin values precedence")
	void rawModeKeepsCustomCodecAndOriginValueSemantics() {
		TapCodecsRegistry registry = new TapCodecsRegistry();
		registry.registerToTapValue(Double.class, new ToTapValueCodec<TapRawValue>() {
			@Override
			public TapRawValue toTapValue(Object value, io.tapdata.entity.schema.type.TapType tapType) {
				return new TapRawValue("custom");
			}
		});
		TapCodecsFilterManager filterManager = TapCodecsFilterManager.create(registry);
		Map<String, TapField> nameFieldMap = new LinkedHashMap<>();
		nameFieldMap.put("floatField", field("floatField", "float").tapType(new TapFloat()));

		Map<String, Object> customRecord = new LinkedHashMap<>();
		customRecord.put("floatField", 1.25d);
		filterManager.transformToTapValueMap(FloatingPointTransformMode.RAW, customRecord, nameFieldMap);
		assertInstanceOf(TapRawValue.class, customRecord.get("floatField"));

		TapCodecsFilterManager originFilterManager = TapCodecsFilterManager.create(new TapCodecsRegistry());
		Map<String, Object> originRecord = new LinkedHashMap<>();
		originRecord.put("floatField", 1.25d);
		Map<String, TapField> originFieldMap = new LinkedHashMap<>();
		originFieldMap.put("floatField", field("floatField", "float").tapType(new TapFloat() {
			@Override
			public ToTapValueCodec<?> toTapValueCodec() {
				return new io.tapdata.entity.codec.impl.ToTapFloatCodec();
			}
		}));
		Map<String, TapValue<?, ?>> originMap = new LinkedHashMap<>();
		TapFloatValue originValue = new TapFloatValue(1.25d);
		originValue.setOriginValue("1.25");
		originMap.put("floatField", originValue);
		originFilterManager.transformToTapValueMap(FloatingPointTransformMode.RAW, originRecord, originFieldMap, originMap);
		assertInstanceOf(TapFloatValue.class, originRecord.get("floatField"));
		assertEquals("1.25", ((TapValue<?, ?>) originRecord.get("floatField")).getOriginValue());
	}

	@Test
	@DisplayName("schema-enforced raw mode does not mark raw floating-point fields for reverse TapValue conversion")
	void schemaEnforcedRawModeLeavesRawFloatingPointFieldsUntouched() {
		TapTable table = new TapTable("floating-point-table");
		table.add(field("floatField", "float").tapType(new TapFloat()));
		table.add(field("doubleField", "double").tapType(new TapDouble()));
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("floatField", 1.25d);
		record.put("doubleField", 2.5f);

		TapCodecsFilterManagerSchemaEnforced filterManager = new TapCodecsFilterManagerSchemaEnforced(new TapCodecsRegistry());
		Set<String> transformed = filterManager.transformToTapValueMap(FloatingPointTransformMode.RAW, record, table);

		assertTrue(transformed.isEmpty());
		assertInstanceOf(Double.class, record.get("floatField"));
		assertEquals((double) 1.25f, record.get("floatField"));
		assertInstanceOf(Double.class, record.get("doubleField"));
		assertEquals(2.5d, record.get("doubleField"));
	}

	@Test
	@DisplayName("schema-enforced manager reuses the field-level schema codec plan")
	void schemaEnforcedManagerCachesSchemaCodecPlan() {
		CountingTapFloat type = new CountingTapFloat();
		TapTable table = new TapTable("cached-plan-table");
		table.add(field("floatField", "float").tapType(type));
		TapCodecsFilterManagerSchemaEnforced filterManager = new TapCodecsFilterManagerSchemaEnforced(new TapCodecsRegistry());

		Map<String, Object> firstRecord = new LinkedHashMap<>();
		firstRecord.put("floatField", 1.25d);
		filterManager.transformToTapValueMap(firstRecord, table);
		Map<String, Object> secondRecord = new LinkedHashMap<>();
		secondRecord.put("floatField", 2.5d);
		filterManager.transformToTapValueMap(secondRecord, table);

		assertEquals(1, type.getCodecLookups());
		assertInstanceOf(TapFloatValue.class, firstRecord.get("floatField"));
		assertInstanceOf(TapFloatValue.class, secondRecord.get("floatField"));
	}

	private static class CountingTapFloat extends TapFloat {
		private int codecLookups;

		@Override
		public ToTapValueCodec<?> toTapValueCodec() {
			codecLookups++;
			return new io.tapdata.entity.codec.impl.ToTapFloatCodec();
		}

		private int getCodecLookups() {
			return codecLookups;
		}
	}

	private static class ExposedFilterManager extends TapCodecsFilterManager {
		private ExposedFilterManager() {
			super(new TapCodecsRegistry());
		}

		private ToTapValueCodec<?> valueCodec(TapType type) {
			return getValueCodec(type);
		}
	}
}

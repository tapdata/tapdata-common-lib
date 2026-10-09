package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.type.TapCoefficientFloat;
import io.tapdata.entity.schema.type.TapDouble;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapNumber;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

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

	private static class ExposedFilterManager extends TapCodecsFilterManager {
		private ExposedFilterManager() {
			super(new TapCodecsRegistry());
		}

		private ToTapValueCodec<?> valueCodec(TapType type) {
			return getValueCodec(type);
		}
	}
}

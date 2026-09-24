package io.tapdata.entity.codec.filter;

import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.schema.type.TapFloat;
import io.tapdata.entity.schema.type.TapType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

	private static class ExposedFilterManager extends TapCodecsFilterManager {
		private ExposedFilterManager() {
			super(new TapCodecsRegistry());
		}

		private ToTapValueCodec<?> valueCodec(TapType type) {
			return getValueCodec(type);
		}
	}
}

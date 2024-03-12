package io.tapdata.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnitTestUtilsTest {


	@Test
	void testIsTesting() {
		Assertions.assertTrue(UnitTestUtils.isTesting());
	}

	@Nested
	class InjectFieldTest {

		@Test
		void shouldSetFieldValue() {
			// Given
			TestClass testClass = new TestClass();
			String fieldName = "field";
			String expectedValue = "newValue";

			// When
			UnitTestUtils.injectField(TestClass.class, testClass, fieldName, expectedValue);

			// Then
			Assertions.assertEquals(expectedValue, testClass.getField());
		}

		@Test
		void shouldThrowExceptionWhenFieldDoesNotExist() {
			// Given
			TestClass testClass = new TestClass();
			String fieldName = "nonExistentField";
			String value = "newValue";

			// Then
			Assertions.assertThrows(RuntimeException.class, () -> {
				// When
				UnitTestUtils.injectField(TestClass.class, testClass, fieldName, value);
			});
		}

		@Test
		void shouldThrowExceptionWhenValueIsIncompatible() {
			// Given
			TestClass testClass = new TestClass();
			String fieldName = "field";
			int incompatibleValue = 123;

			// Then
			Assertions.assertThrows(RuntimeException.class, () -> {
				// When
				UnitTestUtils.injectField(TestClass.class, testClass, fieldName, incompatibleValue);
			});
		}

		class TestClass {
			private String field;

			public String getField() {
				return field;
			}
		}
	}
}

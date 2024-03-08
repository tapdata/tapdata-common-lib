package io.tapdata.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnitTestUtilsTest {


	@Test
	void testIsTesting() {
		Assertions.assertTrue(UnitTestUtils.isTesting());
	}
}

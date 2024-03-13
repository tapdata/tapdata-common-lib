package io.tapdata.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

/**
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/3/5 16:44 Create
 */
public class AppTypeTest {

	MockedStatic<AppType> mock() throws Exception {
		MockedStatic<AppType> appTypeMockedStatic = Mockito.mockStatic(AppType.class, CALLS_REAL_METHODS);
		try {
			Field instance = AppType.class.getDeclaredField("instance");
			instance.setAccessible(true);
			instance.set(null, null);
			return appTypeMockedStatic;
		} catch (Exception e) {
			appTypeMockedStatic.close();
			throw e;
		}
	}

	@Nested
	class EnumTest {
		@Test
		void testIsDrs() {
			Assertions.assertTrue(AppType.DRS.isDrs());
			Assertions.assertFalse(AppType.DFS.isDrs());
			Assertions.assertFalse(AppType.DAAS.isDrs());
		}

		@Test
		void testIsDfs() {
			Assertions.assertFalse(AppType.DRS.isDfs());
			Assertions.assertTrue(AppType.DFS.isDfs());
			Assertions.assertFalse(AppType.DAAS.isDfs());
		}

		@Test
		void testIsDaas() {
			Assertions.assertFalse(AppType.DRS.isDaas());
			Assertions.assertFalse(AppType.DFS.isDaas());
			Assertions.assertTrue(AppType.DAAS.isDaas());
		}

		@Test
		void testIsCloud() {
			Assertions.assertTrue(AppType.DRS.isCloud());
			Assertions.assertTrue(AppType.DFS.isCloud());
			Assertions.assertFalse(AppType.DAAS.isCloud());
		}
	}

	@Nested
	class InitTest {

		@Test
		void testInTesting() throws Exception {
			try (MockedStatic<AppType> ignore = mock()) {
				// first to init instance
				Assertions.assertTrue(AppType.DAAS == AppType.currentType());
				// second to get instance
				Assertions.assertTrue(AppType.DAAS == AppType.currentType());
			}
		}

		@Test
		void testAppTypeBlank() throws Exception {
			String appTypeStr = "ERROR TYPE";
			String errorMsg = "app_type is blank";
			try (MockedStatic<UnitTestUtils> testUtilsMockedStatic = Mockito.mockStatic(UnitTestUtils.class, CALLS_REAL_METHODS)) {
				testUtilsMockedStatic.when(UnitTestUtils::isTesting).thenReturn(false);
				try (MockedStatic<AppType> ignore = mock()) {
					AppType.currentType();
					Assertions.fail("Expect to throw an exception: " + errorMsg);
				} catch (RuntimeException e) {
					Assertions.assertTrue(errorMsg.equals(e.getMessage()));

					System.setProperty("app_type", appTypeStr);
					try {
						AppType.currentType();
					} catch (RuntimeException ex) {
						Assertions.assertTrue(("nonsupport app_type: " + appTypeStr).equals(ex.getMessage()));
					}
				}
			}
		}
	}
}

package io.tapdata.utils;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

/**
 *
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/3/5 16:44 Create
 */
public class AppTypeTest {

	public static <T> T callInType(Callable<T> callable, AppType appType, AppType... otherTypes) throws Exception {
		try (MockedStatic<AppType> mocked = Mockito.mockStatic(AppType.class, CALLS_REAL_METHODS)) {
			if (null == otherTypes) {
				mocked.when(AppType::currentType).thenReturn(appType);
			} else {
				mocked.when(AppType::currentType).thenReturn(appType, otherTypes);
			}

			return callable.call();
		}
	}
}

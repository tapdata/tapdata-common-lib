package io.tapdata.utils;

import java.lang.reflect.Field;

/**
 * 单测工具
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/3/5 13:53 Create
 */
public class UnitTestUtils {

	private static Boolean isTesting;

	private static synchronized void initIsTesting() {
		if (null != isTesting) return;

		String execCommand = System.getProperty("sun.java.command", "");
		if (
			execCommand.startsWith("com.intellij.rt.junit.JUnitStarter")
			|| execCommand.startsWith("org.apache.maven.surefire.booter.ForkedBooter")
		) {
			isTesting = true;
			return;
		}

		isTesting = false;
	}

	private UnitTestUtils() {
	}

	public static boolean isTesting() {
		if (null == isTesting) {
			initIsTesting();
		}

		return isTesting;
	}

	public static <T> void injectField(Class<T> clz, T ins, String field, Object value) {
		try {
			Field declaredField = clz.getDeclaredField(field);
			declaredField.setAccessible(true);
			declaredField.set(ins, value);
		} catch (Exception e) {
			throw new RuntimeException("Inject value to " + clz.getSimpleName() + "." + field + " failed", e);
		}
	}
}

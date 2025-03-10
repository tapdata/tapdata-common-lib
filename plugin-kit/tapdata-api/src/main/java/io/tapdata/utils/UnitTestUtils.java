package io.tapdata.utils;

import java.io.PrintWriter;
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

		String property = System.getProperty("java.version","");
		boolean flagForJdk8 = checkForJdk8(property);
		boolean flagForJdk11 = checkForJdk11(property);
		boolean checkForJdk17 = checkForJdk17(property);
		String execCommand = System.getProperty("sun.java.command", "");
		if (
			execCommand.startsWith("com.intellij.rt.junit.JUnitStarter")
			|| execCommand.startsWith("org.apache.maven.surefire.booter.ForkedBooter") || flagForJdk11 || flagForJdk8 || checkForJdk17
		) {
			isTesting = true;
			return;
		}

		isTesting = false;
	}

	private static boolean checkForJdk8(String property) {
		if (property.startsWith("1.8.")) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				String methodName = stackTraceElement.getClassName();
				if (methodName.contains("com.intellij.junit5.JUnit5IdeaTestRunner") || methodName.contains("org.apache.maven.surefire.junitplatform.JUnitPlatformProvider")) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean checkForJdk11(String property) {
		if (property.startsWith("11.")) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				String methodName = stackTraceElement.getClassName();
				if (methodName.contains("com.intellij.junit5.JUnit5IdeaTestRunner") || methodName.contains("org.apache.maven.surefire.junitplatform.JUnitPlatformProvider")) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean checkForJdk17(String property) {
		if (property.startsWith("17.")) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				String methodName = stackTraceElement.getClassName();
				if (methodName.contains("com.intellij.junit5.JUnit5IdeaTestRunner") || methodName.contains("org.apache.maven.surefire.junitplatform.JUnitPlatformProvider")) {
					return true;
				}
			}
		}
		return false;
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

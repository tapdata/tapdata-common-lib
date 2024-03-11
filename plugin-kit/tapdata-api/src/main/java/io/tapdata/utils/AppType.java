package io.tapdata.utils;


/**
 * 应用类型
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/3/5 15:23 Create
 */
public enum AppType {
	DRS, DFS, DAAS,
	;

	private static final String ARGS_NAME = "app_type";

	public boolean isDrs() {
		return DRS == this;
	}

	public boolean isDfs() {
		return DFS == this;
	}

	public boolean isDaas() {
		return DAAS == this;
	}

	public boolean isCloud() {
		return DRS == this || DFS == this;
	}


	private static AppType instance;

	public static AppType currentType() {
		if (null != instance) {
			return instance;
		}
		synchronized (AppType.class) {
			instance = init();
		}
		return instance;
	}

	private static AppType init() {
		String appTypeStr;
		if (UnitTestUtils.isTesting()) {
			appTypeStr = System.getenv(ARGS_NAME);
			if (null == appTypeStr) {
				appTypeStr = System.getProperty(ARGS_NAME, AppType.DAAS.name());
			}
		} else {
			appTypeStr = System.getenv(ARGS_NAME);
			if (null == appTypeStr) {
				appTypeStr = System.getProperty(ARGS_NAME, "");
			}
		}

		appTypeStr = appTypeStr.trim().toUpperCase();
		if (appTypeStr.isEmpty()) {
			throw new RuntimeException(ARGS_NAME + " is blank");
		}

		AppType appType;
		try {
			appType = AppType.valueOf(appTypeStr);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("nonsupport " + ARGS_NAME + ": " + appTypeStr);
		}
		return appType;
	}
}

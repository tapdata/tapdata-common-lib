package io.tapdata.pdk.core.tapnode;

import io.tapdata.ErrorCodeConfig;
import io.tapdata.Scanner;
import io.tapdata.entity.error.CoreException;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.exception.TapExClass;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author samuel
 * @Description
 * @create 2024-10-29 16:12
 **/
public class TapErrorCodeAnnotationHandler extends TapBaseAnnotationHandler {
	private static final String TAG = TapErrorCodeAnnotationHandler.class.getSimpleName();

	@Override
	public void handle(Set<Class<?>> classes) throws CoreException {
		if (null == classes || classes.isEmpty()) {
			return;
		}
		TapLogger.debug(TAG, "--------------" + TAG + " Classes Start------------- size {}", classes.size());
		for (Class<?> clazz : classes) {
			TapExClass tapExCLassAnnotation = clazz.getAnnotation(TapExClass.class);
			if (null == tapExCLassAnnotation) {
				continue;
			}
			Scanner.scanClassExCode(clazz, errorCodeEntity -> ErrorCodeConfig.getInstance().putErrorCode(errorCodeEntity));
		}
	}

	@Override
	public Class<? extends Annotation> watchAnnotation() {
		return TapExClass.class;
	}
}

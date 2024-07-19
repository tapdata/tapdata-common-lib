package io.tapdata.common.concurrent;

/**
 * @author samuel
 * @Description
 * @create 2024-07-19 22:37
 **/
public class TapExecutors {
	public static final String TAG = TapExecutors.class.getSimpleName();

	public static <T, R> SimpleConcurrentProcessorImpl<T, R> createSimple(int thread, int queueSize, String tag) {
		return new SimpleConcurrentProcessorImpl<>(thread, queueSize, tag);
	}
}

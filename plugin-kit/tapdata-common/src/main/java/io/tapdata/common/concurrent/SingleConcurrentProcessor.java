package io.tapdata.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author samuel
 * @Description
 * @create 2024-07-19 22:22
 **/
public class SingleConcurrentProcessor<T, R> extends BaseConcurrentProcessor<T, R> {
	public static final String TAG = SingleConcurrentProcessor.class.getSimpleName();

	public SingleConcurrentProcessor(int thread, int queueSize, String tag) {
		super(thread, queueSize, tag);
	}

	@Override
	String getTag() {
		return TAG;
	}

	@Override
	public void runAsync(T input, Function<T, R> function) {
		super.produce(input, function);
	}

	@Override
	public boolean runAsync(T input, Function<T, R> function, long timeout, TimeUnit timeUnit) {
		return super.produce(input, function, timeout, timeUnit);
	}

	@Override
	public void close() {

	}
}

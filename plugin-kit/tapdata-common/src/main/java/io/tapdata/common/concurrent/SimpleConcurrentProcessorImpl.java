package io.tapdata.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author samuel
 * @Description
 * @create 2024-07-19 22:22
 **/
public class SimpleConcurrentProcessorImpl<T, R> extends BaseConcurrentProcessor<T, R> {
	public static final String TAG = SimpleConcurrentProcessorImpl.class.getSimpleName();

	public SimpleConcurrentProcessorImpl(int thread, int queueSize, String tag) {
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
	public void runAsyncWithBlocking(T input, Function<T, R> function) {
		synchronized (this.produceLock) {
			super.produce(input, function);
			super.putBarrierInAllProducerQueue();
		}
	}

	@Override
	public boolean runAsyncWithBlocking(T input, Function<T, R> function, long timeout, TimeUnit timeUnit) {
		synchronized (this.produceLock) {
			boolean produce = super.produce(input, function, timeout, timeUnit);
			if (Boolean.TRUE.equals(produce)) {
				super.putBarrierInAllProducerQueue();
				return true;
			}
		}
		return false;
	}
}

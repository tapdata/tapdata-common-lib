package io.tapdata.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author samuel
 * @Description
 * @create 2024-07-19 22:20
 **/
public interface ConcurrentProcessor<T, R> {
	void start();

	void runAsync(T input, Function<T, R> function);

	boolean runAsync(T input, Function<T, R> function, long timeout, TimeUnit timeUnit);

	R get();

	R get(long timeout, TimeUnit timeUnit);

	void close();
}

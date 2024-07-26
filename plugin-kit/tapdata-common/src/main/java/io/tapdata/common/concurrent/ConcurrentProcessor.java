package io.tapdata.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author samuel
 * @Description Concurrent processor implemented using queues
 * Usage scenarios: Suitable for scenarios where the processing can be out of order, multi-threading is used to accelerate, and the final result is in order.
 * The order is consistent with the input order.
 * Usage example:
 *
 * SimpleConcurrentProcessorImpl<Object, Object> test = TapExecutors.createSimple(4, 100, "test");
 * test.start();
 * // Create a thread and call get() to obtain the processed object
 * new Thread(()->{
 * 	while(true){
 * 		Object r = test.get();
 * 	}
 * }).start()
 * // Call runAsync and pass in the object and processing method that need to be processed.
 * test.runAsync("test", o -> {
 * 	try {
 * 		TimeUnit.MILLISECONDS.sleep(10L);
 * 	} catch (InterruptedException e) {
 * 		throw new RuntimeException(e);
 * 	}
 * 	o = o + "-x";
 * 	return o;
 * });
 * 
 * @create 2024-07-19 22:20
 **/
public interface ConcurrentProcessor<T, R> {
	void start();

	void runAsync(T input, Function<T, R> function);

	boolean runAsync(T input, Function<T, R> function, long timeout, TimeUnit timeUnit);

	void runAsyncWithBlocking(T input, Function<T, R> function);

	boolean runAsyncWithBlocking(T input, Function<T, R> function, long timeout, TimeUnit timeUnit);

	R get();

	R get(long timeout, TimeUnit timeUnit);

	void pause();

	void resume();

	void close();
}

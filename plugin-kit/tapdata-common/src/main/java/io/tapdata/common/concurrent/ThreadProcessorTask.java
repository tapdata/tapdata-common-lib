package io.tapdata.common.concurrent;

import java.util.function.Function;

/**
 * @author samuel
 * @Description
 * @create 2024-07-25 17:22
 **/
public class ThreadProcessorTask<T, R> extends ThreadTask<T, R> {
	private final Function<T, R> processor;
	private final T input;

	public ThreadProcessorTask(Function<T, R> processor, T input) {
		this.processor = processor;
		this.input = input;
	}

	public Function<T, R> getProcessor() {
		return processor;
	}

	public T getInput() {
		return input;
	}
}

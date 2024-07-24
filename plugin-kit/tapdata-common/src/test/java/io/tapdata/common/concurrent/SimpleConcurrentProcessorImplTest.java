package io.tapdata.common.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author samuel
 * @Description
 * @create 2024-07-22 14:35
 **/
@DisplayName("Class SimpleConcurrentProcessorImpl Test")
class SimpleConcurrentProcessorImplTest {
	@Test
	@DisplayName("test main process by calling methods: runAsync(), get()")
	@Order(1)
	void test1() {
		List<Integer> list = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(10000);
		IntStream.range(0, Long.valueOf(countDownLatch.getCount()).intValue()).forEach(list::add);
		SimpleConcurrentProcessorImpl<Integer, Integer> processor = new SimpleConcurrentProcessorImpl<>(4, 10, "test");
		processor.start();
		List<Integer> result = new ArrayList<>();
		new Thread(() -> {
			for (Integer i : list) {
				processor.runAsync(i, input -> input);
			}
		}).start();
		new Thread(() -> {
			while (countDownLatch.getCount() >= 0L) {
				result.add(processor.get());
				countDownLatch.countDown();
			}
		}).start();
		assertDoesNotThrow(() -> countDownLatch.await());
		processor.close();
		assertEquals(list, result);
	}

	@Test
	@DisplayName("test main process by calling methods with timeout input parameters: runAsync(timeout, unit), get(timeout, unit)")
	@Order(2)
	void test2() {
		List<Integer> list = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(10000);
		IntStream.range(0, Long.valueOf(countDownLatch.getCount()).intValue()).forEach(list::add);
		SimpleConcurrentProcessorImpl<Integer, Integer> processor = new SimpleConcurrentProcessorImpl<>(4, 10, "test");
		processor.start();
		List<Integer> result = new ArrayList<>();
		new Thread(() -> {
			for (Integer i : list) {
				while (true) {
					boolean b = processor.runAsync(i, input -> input, 1L, TimeUnit.SECONDS);
					if (b) {
						break;
					}
				}
			}
		}).start();
		new Thread(() -> {
			while (countDownLatch.getCount() >= 0L) {
				Integer i = processor.get(1L, TimeUnit.SECONDS);
				if (null != i) {
					result.add(i);
					countDownLatch.countDown();
				}
			}
		}).start();
		assertDoesNotThrow(() -> countDownLatch.await());
		processor.close();
		assertEquals(list, result);
	}
}
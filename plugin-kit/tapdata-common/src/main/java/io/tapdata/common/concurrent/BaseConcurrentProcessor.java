package io.tapdata.common.concurrent;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author samuel
 * @Description
 * @create 2024-07-19 22:38
 **/
public abstract class BaseConcurrentProcessor<T, R> implements ConcurrentProcessor<T, R> {
	protected final int thread;
	protected final int queueSize;
	protected final String tag;
	protected final BlockingQueue<ThreadTask<T, R>>[] producerQueue;
	protected final BlockingQueue<R>[] consumerQueue;
	protected final AtomicBoolean running;
	protected int producerIndex;
	protected int consumerIndex;
	protected final int[] produceLock;
	protected final int[] consumeLock;
	protected ThreadPoolExecutor consumerThreadPool;
	protected CompletableFuture<Void>[] consumerFutures;

	public BaseConcurrentProcessor(int thread, int queueSize, String tag) {
		this.thread = thread;
		this.queueSize = queueSize;
		this.tag = tag;
		this.producerQueue = new LinkedBlockingQueue[thread];
		this.consumerQueue = new LinkedBlockingQueue[thread];
		IntStream.range(0, thread).forEach(i -> {
			this.producerQueue[i] = new LinkedBlockingQueue<>(queueSize);
			this.consumerQueue[i] = new LinkedBlockingQueue<>(queueSize);
		});
		this.running = new AtomicBoolean(false);
		this.produceLock = new int[0];
		this.consumeLock = new int[0];
		this.consumerFutures = new CompletableFuture[thread];
	}

	@Override
	public void start() {
		if (running.compareAndSet(false, true)) {
			this.consumerThreadPool = new ThreadPoolExecutor(
					thread,
					thread,
					0L,
					TimeUnit.MILLISECONDS,
					new SynchronousQueue<>(),
					new ThreadFactory() {
						final AtomicInteger index = new AtomicInteger(0);

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.join("-", getTag(), "consumer", "thread", tag, String.valueOf(index.getAndIncrement())));
						}
					}
			);
			for (int i = 0; i < thread; i++) {
				consumerFutures[i] = CompletableFuture.completedFuture(i).thenAcceptAsync(index -> {
					try {
						while (running.get()) {
							ThreadTask<T, R> threadTask = producerQueue[index].take();
							T input = threadTask.getInput();
							Function<T, R> processor = threadTask.getProcessor();
							R apply = processor.apply(input);
							consumerQueue[index].put(apply);
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}, consumerThreadPool);
			}
		}
	}

	protected void produce(T t, Function<T, R> function) {
		if (null == t || null == function) {
			return;
		}
		start();
		BlockingQueue<ThreadTask<T, R>> producerQueue = this.producerQueue[producerIndex];
		try {
			synchronized (this.produceLock) {
				ThreadTask<T, R> threadTask = new ThreadTask<>(function, t);
				producerQueue.put(threadTask);
				producerIndex = (producerIndex + 1) % thread;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	protected boolean produce(T t, Function<T, R> function, long timeout, TimeUnit timeUnit) {
		if (null == t || null == function) {
			return true;
		}
		start();
		BlockingQueue<ThreadTask<T, R>> producerQueue = this.producerQueue[producerIndex];
		boolean offered = false;
		synchronized (this.produceLock) {
			try {
				ThreadTask<T, R> threadTask = new ThreadTask<>(function, t);
				offered = producerQueue.offer(threadTask, timeout, timeUnit);
				if (offered) {
					producerIndex = (producerIndex + 1) % thread;
					return true;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return offered;
	}

	abstract String getTag();

	@Override
	public R get() {
		synchronized (this.consumeLock) {
			try {
				R take = consumerQueue[consumerIndex].take();
				consumerIndex = (consumerIndex + 1) % thread;
				return take;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	@Override
	public R get(long timeout, TimeUnit timeUnit) {
		synchronized (this.consumeLock) {
			try {
				R poll = consumerQueue[consumerIndex].poll(timeout, timeUnit);
				if (null != poll) {
					consumerIndex = (consumerIndex + 1) % thread;
					return poll;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	protected static class ThreadTask<T, R> {
		private final Function<T, R> processor;
		private final T input;

		public ThreadTask(Function<T, R> processor, T input) {
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

	@Override
	public void close() {
		this.running.set(false);
		Optional.ofNullable(consumerFutures).ifPresent(futures -> {
			for (CompletableFuture<Void> future : futures) {
				future.cancel(true);
			}
		});
		Optional.ofNullable(consumerThreadPool).ifPresent(ThreadPoolExecutor::shutdownNow);
	}
}

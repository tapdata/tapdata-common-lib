package io.tapdata.common.concurrent;

import io.tapdata.common.concurrent.exception.ConcurrentProcessorApplyException;

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
	protected final BlockingQueue<ApplyValue>[] consumerQueue;
	protected final AtomicBoolean running;
	protected final AtomicBoolean pause;
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
		this.pause = new AtomicBoolean(false);
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
							if (threadTask instanceof ThreadProcessorTask) {
								ThreadProcessorTask<T, R> processorTask = (ThreadProcessorTask<T, R>) threadTask;
								T input = processorTask.getInput();
								Function<T, R> processor = processorTask.getProcessor();
								Object apply;
								ApplyValue applyValue;
								try {
									apply = processor.apply(input);
									if (null == apply) {
										applyValue = new ApplyValue(null);
									} else {
										applyValue = new ApplyValue(apply);
									}
								} catch (Exception e) {
									applyValue = new ApplyValue(input, e);
								}
								consumerQueue[index].put(applyValue);
							} else if (threadTask instanceof ThreadBarrierTask) {
								ThreadBarrierTask<T, Object> threadBarrierTask = (ThreadBarrierTask<T, Object>) threadTask;
								CyclicBarrier cyclicBarrier = threadBarrierTask.getCyclicBarrier();
								if (null == cyclicBarrier) {
									continue;
								}
								try {
									cyclicBarrier.await();
								} catch (BrokenBarrierException e) {
									Thread.currentThread().interrupt();
									break;
								}
							}

						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}, consumerThreadPool);
			}
		}
	}

	protected void produce(T t, Function<T, R> function) {
		if (null == t) {
			return;
		}
		if (null == function) {
			throw new IllegalArgumentException("Process function is null");
		}
		start();
		try {
			pauseWaitIfNeed();
			synchronized (this.produceLock) {
				ThreadTask<T, R> threadTask = new ThreadProcessorTask<>(function, t);
				this.producerQueue[producerIndex].put(threadTask);
				producerIndex = (producerIndex + 1) % thread;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	protected boolean produce(T t, Function<T, R> function, long timeout, TimeUnit timeUnit) {
		if (null == t) {
			return true;
		}
		if (null == function) {
			throw new IllegalArgumentException("Process function is null");
		}
		start();
		boolean offered = false;
		try {
			pauseWaitIfNeed();
			synchronized (this.produceLock) {
				BlockingQueue<ThreadTask<T, R>> producerQueue = this.producerQueue[producerIndex];

				ThreadTask<T, R> threadTask = new ThreadProcessorTask<>(function, t);
				offered = producerQueue.offer(threadTask, timeout, timeUnit);
				if (offered) {
					producerIndex = (producerIndex + 1) % thread;
				}

			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return offered;
	}

	abstract String getTag();

	@Override
	public R get() throws ConcurrentProcessorApplyException {
		synchronized (this.consumeLock) {
			try {
				ApplyValue take = consumerQueue[consumerIndex].take();
				consumerIndex = (consumerIndex + 1) % thread;
				if (null != take.getException()) {
					throw new ConcurrentProcessorApplyException(take.getException(), take.getValue());
				}
				return (R) take.getValue();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	@Override
	public R get(long timeout, TimeUnit timeUnit) throws ConcurrentProcessorApplyException {
		ApplyValue result;
		synchronized (this.consumeLock) {
			try {
				result = consumerQueue[consumerIndex].poll(timeout, timeUnit);
				if (null != result) {
					consumerIndex = (consumerIndex + 1) % thread;
					if (null != result.getException()) {
						throw new ConcurrentProcessorApplyException(result.getException(), result.getValue());
					} else {
						return (R) result.getValue();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	@Override
	public void close() {
		resume();
		this.running.set(false);
		Optional.ofNullable(consumerFutures).ifPresent(futures -> {
			for (CompletableFuture<Void> future : futures) {
				Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			}
		});
		Optional.ofNullable(consumerThreadPool).ifPresent(ThreadPoolExecutor::shutdownNow);
	}

	protected void putBarrierInAllProducerQueue() {
		try {
			synchronized (this.produceLock) {
				CyclicBarrier cyclicBarrier = new CyclicBarrier(thread);
				for (BlockingQueue<ThreadTask<T, R>> queue : this.producerQueue) {
					ThreadTask<T, R> threadTask = new ThreadBarrierTask<>(cyclicBarrier);
					queue.put(threadTask);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void pauseWaitIfNeed() throws InterruptedException {
		if (Boolean.TRUE.equals(pause.get())) {
			synchronized (this.pause) {
				this.pause.wait();
			}
		}
	}

	@Override
	public synchronized void pause() {
		this.pause.set(true);
	}

	@Override
	public void resume() {
		if (pause.compareAndSet(true, false)) {
			synchronized (this.pause) {
				this.pause.notify();
			}
		}
	}
}
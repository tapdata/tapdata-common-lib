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
	protected BlockingQueue<ThreadTask<T, R>>[] producerQueue;
	protected BlockingQueue<ApplyValue>[] consumerQueue;
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
		this.producerQueue = new ArrayBlockingQueue[thread];
		this.consumerQueue = new ArrayBlockingQueue[thread];
		IntStream.range(0, thread).forEach(i -> {
			this.producerQueue[i] = new ArrayBlockingQueue<>(queueSize);
			this.consumerQueue[i] = new ArrayBlockingQueue<>(queueSize);
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
		// Resume any paused operations to allow proper shutdown
		resume();

		// Set running flag to false to stop worker threads
		this.running.set(false);

		// Cancel all consumer futures with interruption
		try {
			Optional.ofNullable(consumerFutures).ifPresent(futures -> {
				for (CompletableFuture<Void> future : futures) {
					Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
				}
			});
		} catch (Exception e) {
			// Ignore exceptions during future cancellation
		}

		// Shutdown thread pool with timeout
		try {
			Optional.ofNullable(consumerThreadPool).ifPresent(pool -> {
				pool.shutdown();
				try {
					// Wait for graceful shutdown for 1 second
					if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
						// Force shutdown if graceful shutdown failed
						pool.shutdownNow();
						// Wait for forced shutdown for another 1 second
						if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
							// Log warning if shutdown still not complete (optional)
						}
					}
				} catch (InterruptedException e) {
					// Force shutdown on interruption
					pool.shutdownNow();
					Thread.currentThread().interrupt();
				}
			});
		} catch (Exception e) {
			// Ignore exceptions during thread pool shutdown
		}

		// Clear all queues to release memory
		try {
			Optional.ofNullable(producerQueue).ifPresent(queues -> {
				for (BlockingQueue<ThreadTask<T, R>> queue : queues) {
					Optional.ofNullable(queue).ifPresent(BlockingQueue::clear);
				}
			});
		} catch (Exception e) {
			// Ignore exceptions during producer queue cleanup
		}

		try {
			Optional.ofNullable(consumerQueue).ifPresent(queues -> {
				for (BlockingQueue<ApplyValue> queue : queues) {
					Optional.ofNullable(queue).ifPresent(BlockingQueue::clear);
				}
			});
		} catch (Exception e) {
			// Ignore exceptions during consumer queue cleanup
		}

		// Clear references to help GC
		try {
			this.consumerThreadPool = null;
			this.consumerFutures = null;
		} catch (Exception e) {
			// Ignore exceptions during reference cleanup
		}
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
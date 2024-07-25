package io.tapdata.common.concurrent;

import java.util.concurrent.CyclicBarrier;

/**
 * @author samuel
 * @Description
 * @create 2024-07-25 17:21
 **/
public class ThreadBarrierTask<T, R> extends ThreadTask<T,R> {
	private CyclicBarrier cyclicBarrier;

	public ThreadBarrierTask(CyclicBarrier cyclicBarrier) {
		this.cyclicBarrier = cyclicBarrier;
	}

	public CyclicBarrier getCyclicBarrier() {
		return cyclicBarrier;
	}
}

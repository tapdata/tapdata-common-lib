package io.tapdata.common.utils;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author samuel
 * @Description
 * @create 2024-07-06 14:44
 **/
public class StopWatch {

	/**
	 * 创建计时任务（秒表）
	 *
	 * @param id 用于标识秒表的唯一ID
	 * @return StopWatch
	 */
	public static StopWatch create(String id) {
		return new StopWatch(id);
	}

	/**
	 * 秒表唯一标识，用于多个秒表对象的区分
	 */
	private final String id;
	private boolean keepTaskInfo = true;
	private Map<String, TaskInfo> taskMap;

	/**
	 * 任务名称
	 */
	private String currentTaskName;
	/**
	 * 开始时间
	 */
	private long startTimeNanos;

	/**
	 * 最后一次任务对象
	 */
	private TaskInfo lastTaskInfo;
	/**
	 * 总任务数
	 */
	private int taskCount;
	/**
	 * 总运行时间
	 */
	private long totalTimeNanos;
	// ------------------------------------------------------------------------------------------- Constructor start

	/**
	 * 构造，不启动任何任务
	 */
	public StopWatch() {
		this("");
	}

	/**
	 * 构造，不启动任何任务
	 *
	 * @param id 用于标识秒表的唯一ID
	 */
	public StopWatch(String id) {
		this(id, true);
	}

	/**
	 * 构造，不启动任何任务
	 *
	 * @param id           用于标识秒表的唯一ID
	 * @param keepTaskInfo 是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
	 */
	public StopWatch(String id, boolean keepTaskInfo) {
		this.id = id;
		setKeepTaskInfo(keepTaskInfo);
	}
	// ------------------------------------------------------------------------------------------- Constructor end

	/**
	 * 获取StopWatch 的ID，用于多个秒表对象的区分
	 *
	 * @return the ID 空字符串为
	 * @see #StopWatch(String)
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * 设置是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
	 *
	 * @param keepTaskInfo 是否在停止后保留任务
	 */
	public void setKeepTaskInfo(boolean keepTaskInfo) {
		if (keepTaskInfo) {
			if (null == this.taskMap) {
				this.taskMap = new LinkedHashMap<>();
			}
		} else {
			this.taskMap = null;
		}
		this.keepTaskInfo = keepTaskInfo;
	}

	public boolean isKeepTaskInfo() {
		return keepTaskInfo;
	}

	/**
	 * 开始默认的新任务
	 *
	 * @throws IllegalStateException 前一个任务没有结束
	 */
	public void start() throws IllegalStateException {
		start("");
	}

	/**
	 * 开始指定名称的新任务
	 *
	 * @param taskName 新开始的任务名称
	 * @throws IllegalStateException 前一个任务没有结束
	 */
	public void start(String taskName) throws IllegalStateException {
		if (null != this.currentTaskName) {
			stop();
		}
		this.currentTaskName = taskName;
		this.startTimeNanos = System.nanoTime();
	}

	/**
	 * 停止当前任务
	 *
	 * @throws IllegalStateException 任务没有开始
	 */
	public void stop() throws IllegalStateException {
		if (null == this.currentTaskName) {
			throw new IllegalStateException("Can't stop StopWatch: it's not running");
		}

		final long lastTime = System.nanoTime() - this.startTimeNanos;
		this.totalTimeNanos += lastTime;
		if (null != this.taskMap) {
			this.taskMap.computeIfAbsent(this.currentTaskName, k -> new TaskInfo(this.currentTaskName));
			this.lastTaskInfo = this.taskMap.computeIfPresent(this.currentTaskName, (k, v) -> {
				v.timeNanos += lastTime;
				return v;
			});
			this.taskCount = this.taskMap.size();
		} else {
			this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
			++this.taskCount;
		}
		this.currentTaskName = null;
	}

	/**
	 * 检查是否有正在运行的任务
	 *
	 * @return 是否有正在运行的任务
	 * @see #currentTaskName()
	 */
	public boolean isRunning() {
		return (this.currentTaskName != null);
	}

	/**
	 * 获取当前任务名，{@code null} 表示无任务
	 *
	 * @return 当前任务名，{@code null} 表示无任务
	 * @see #isRunning()
	 */
	public String currentTaskName() {
		return this.currentTaskName;
	}

	/**
	 * 获取最后任务的花费时间（纳秒）
	 *
	 * @return 任务的花费时间（纳秒）
	 * @throws IllegalStateException 无任务
	 */
	public long getLastTaskTimeNanos() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task interval");
		}
		return this.lastTaskInfo.getTimeNanos();
	}

	/**
	 * 获取最后任务的花费时间（毫秒）
	 *
	 * @return 任务的花费时间（毫秒）
	 * @throws IllegalStateException 无任务
	 */
	public long getLastTaskTimeMillis() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task interval");
		}
		return this.lastTaskInfo.getTimeMillis();
	}

	/**
	 * 获取最后的任务名
	 *
	 * @return 任务名
	 * @throws IllegalStateException 无任务
	 */
	public String getLastTaskName() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task name");
		}
		return this.lastTaskInfo.getTaskName();
	}

	/**
	 * 获取最后的任务对象
	 *
	 * @return {@link TaskInfo} 任务对象，包括任务名和花费时间
	 * @throws IllegalStateException 无任务
	 */
	public TaskInfo getLastTaskInfo() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task info");
		}
		return this.lastTaskInfo;
	}

	/**
	 * 获取所有任务的总花费时间（纳秒）
	 *
	 * @return 所有任务的总花费时间（纳秒）
	 * @see #getTotalTimeMillis()
	 * @see #getTotalTimeSeconds()
	 */
	public long getTotalTimeNanos() {
		return this.totalTimeNanos;
	}

	/**
	 * 获取所有任务的总花费时间（毫秒）
	 *
	 * @return 所有任务的总花费时间（毫秒）
	 * @see #getTotalTimeNanos()
	 * @see #getTotalTimeSeconds()
	 */
	public long getTotalTimeMillis() {
		return DateUtils.nanosToMillis(this.totalTimeNanos);
	}

	/**
	 * 获取所有任务的总花费时间（秒）
	 *
	 * @return 所有任务的总花费时间（秒）
	 * @see #getTotalTimeNanos()
	 * @see #getTotalTimeMillis()
	 */
	public double getTotalTimeSeconds() {
		return DateUtils.nanosToSeconds(this.totalTimeNanos);
	}

	/**
	 * 获取任务数
	 *
	 * @return 任务数
	 */
	public int getTaskCount() {
		return this.taskCount;
	}

	/**
	 * 获取任务列表
	 *
	 * @return 任务列表
	 */
	public TaskInfo[] getTaskInfo() {
		if (null == this.taskMap) {
			throw new UnsupportedOperationException("Task info is not being kept!");
		}
		return this.taskMap.values().toArray(new TaskInfo[0]);
	}

	/**
	 * 获取任务信息
	 *
	 * @return 任务信息
	 */
	public String shortSummary() {
		return shortSummary(TimeUnit.NANOSECONDS);
	}

	public String shortSummary(TimeUnit timeUnit) {
		String totalTimeStr = this.totalTimeNanos + " ns";
		if (TimeUnit.MILLISECONDS == timeUnit) {
			totalTimeStr = DateUtils.nanosToMillis(this.totalTimeNanos) + " ms";
		} else if (TimeUnit.SECONDS == timeUnit) {
			totalTimeStr = DateUtils.nanosToSeconds(this.totalTimeNanos) + " sec";
		}
		return "StopWatch '" + this.id + "': running time = " + totalTimeStr;
	}

	/**
	 * 生成所有任务的一个任务花费时间表
	 *
	 * @return 任务时间表
	 */
	public String prettyPrint() {
		return prettyPrint(TimeUnit.NANOSECONDS);
	}

	public String prettyPrint(TimeUnit timeUnit) {
		StringBuilder sb = new StringBuilder(shortSummary(timeUnit));
		sb.append(System.lineSeparator());
		if (null == this.taskMap) {
			sb.append("No task info kept");
		} else {
			sb.append("---------------------------------------------").append(System.lineSeparator());
			if (TimeUnit.MILLISECONDS == timeUnit)
				sb.append("ms         %     Task name").append(System.lineSeparator());
			else if (TimeUnit.SECONDS == timeUnit)
				sb.append("sec        %     Task name").append(System.lineSeparator());
			else
				sb.append("ns         %     Task name").append(System.lineSeparator());
			sb.append("---------------------------------------------").append(System.lineSeparator());

			final NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMinimumIntegerDigits(9);
			nf.setGroupingUsed(false);

			final NumberFormat pf = NumberFormat.getPercentInstance();
			pf.setMinimumIntegerDigits(3);
			pf.setGroupingUsed(false);
			for (TaskInfo task : getTaskInfo()) {
				if (TimeUnit.MILLISECONDS == timeUnit) {
					sb.append(nf.format(task.getTimeMillis())).append("  ");
				} else if (TimeUnit.SECONDS == timeUnit) {
					sb.append(nf.format(task.getTimeSeconds())).append("  ");
				} else {
					sb.append(nf.format(task.getTimeNanos())).append("  ");
				}
				sb.append(pf.format((double) task.getTimeNanos() / getTotalTimeNanos())).append("  ");
				sb.append(task.getTaskName()).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(shortSummary());
		if (null != this.taskMap) {
			for (TaskInfo task : this.taskMap.values()) {
				sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeNanos()).append(" ns");
				long percent = Math.round(100.0 * task.getTimeNanos() / getTotalTimeNanos());
				sb.append(" = ").append(percent).append("%");
			}
		} else {
			sb.append("; no task info kept");
		}
		return sb.toString();
	}

	/**
	 * 存放任务名称和花费时间对象
	 *
	 * @author Looly
	 */
	public static final class TaskInfo {

		private final String taskName;
		private long timeNanos;

		public TaskInfo(String taskName) {
			this.taskName = taskName;
		}

		TaskInfo(String taskName, long timeNanos) {
			this.taskName = taskName;
			this.timeNanos = timeNanos;
		}

		/**
		 * 获取任务名
		 *
		 * @return 任务名
		 */
		public String getTaskName() {
			return this.taskName;
		}

		/**
		 * 获取任务花费时间（单位：纳秒）
		 *
		 * @return 任务花费时间（单位：纳秒）
		 * @see #getTimeMillis()
		 * @see #getTimeSeconds()
		 */
		public long getTimeNanos() {
			return this.timeNanos;
		}

		/**
		 * 获取任务花费时间（单位：毫秒）
		 *
		 * @return 任务花费时间（单位：毫秒）
		 * @see #getTimeNanos()
		 * @see #getTimeSeconds()
		 */
		public long getTimeMillis() {
			return DateUtils.nanosToMillis(this.timeNanos);
		}

		/**
		 * 获取任务花费时间（单位：秒）
		 *
		 * @return 任务花费时间（单位：秒）
		 * @see #getTimeMillis()
		 * @see #getTimeNanos()
		 */
		public double getTimeSeconds() {
			return DateUtils.nanosToSeconds(this.timeNanos);
		}
	}
}

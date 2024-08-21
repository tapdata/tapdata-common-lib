package io.tapdata.common.concurrent;

/**
 * @author samuel
 * @Description
 * @create 2024-08-20 18:48
 **/
public class ApplyValue {
	private Object value;
	private Exception exception;

	public ApplyValue(Object value) {
		this.value = value;
	}

	public ApplyValue(Object value, Exception exception) {
		this.value = value;
		this.exception = exception;
	}

	public Object getValue() {
		return value;
	}

	public Exception getException() {
		return exception;
	}
}

package io.tapdata.common.concurrent.exception;

/**
 * @author samuel
 * @Description
 * @create 2024-08-21 12:09
 **/
public class ConcurrentProcessorApplyException extends Exception {
	private Object originValue;

	public ConcurrentProcessorApplyException(Object originValue) {
		this.originValue = originValue;
	}

	public ConcurrentProcessorApplyException(String message, Object originValue) {
		super(message);
		this.originValue = originValue;
	}

	public ConcurrentProcessorApplyException(String message, Throwable cause, Object originValue) {
		super(message, cause);
		this.originValue = originValue;
	}

	public ConcurrentProcessorApplyException(Throwable cause, Object originValue) {
		super(cause);
		this.originValue = originValue;
	}

	public ConcurrentProcessorApplyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object originValue) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.originValue = originValue;
	}

	public Object getOriginValue() {
		return originValue;
	}
}

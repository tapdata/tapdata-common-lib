package io.tapdata.entity.codec.filter.exception;

/**
 * @author samuel
 * @Description
 * @create 2024-07-05 16:31
 **/
public class TapCodecsFilterManagerException extends RuntimeException {
	public TapCodecsFilterManagerException() {
		super();
	}

	public TapCodecsFilterManagerException(String message) {
		super(message);
	}

	public TapCodecsFilterManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public TapCodecsFilterManagerException(Throwable cause) {
		super(cause);
	}

	protected TapCodecsFilterManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

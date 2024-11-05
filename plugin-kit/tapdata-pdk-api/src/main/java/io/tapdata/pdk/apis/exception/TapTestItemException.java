package io.tapdata.pdk.apis.exception;

import io.tapdata.entity.simplify.TapSimplify;
import io.tapdata.exception.TapCodeException;
import io.tapdata.exception.TapRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class TapTestItemException extends TapRuntimeException implements Serializable {
    /**
     * Test item exception
     */
    private String message;
    private String reason;
    private String stack;
    private String solution;
    private String errorCode;

    public TapTestItemException() {
    }

    public TapTestItemException(Throwable cause) {
        if (cause instanceof TapCodeException) {
            TapCodeException tapCodeException = (TapCodeException) cause;
            this.message = tapCodeException.getMessage();
            this.errorCode = tapCodeException.getCode();
        }
        this.stack = TapSimplify.getStackTrace(cause);
    }
    protected TapTestItemException(Throwable cause,String message, String reason, String solution) {
        this.stack = TapSimplify.getStackTrace(cause);
        buildMessage(message, reason, solution);
    }
    protected TapTestItemException(String message, Throwable cause) {
        this.message = message;
    }
    public void buildMessage(String message, String reason, String solution){
        if (StringUtils.isBlank(getMessage())) {
            setMessage(message);
        }
        if (StringUtils.isBlank(getReason())) {
            setReason(reason);
        }
        if (StringUtils.isBlank(getSolution())) {
            setSolution(solution);
        }
    }

    @Override
    protected void clone(TapRuntimeException tapRuntimeException) {
        if (tapRuntimeException instanceof TapTestItemException) {
            TapTestItemException tapTestItemException = (TapTestItemException) tapRuntimeException;
            tapTestItemException.stack = this.stack;
        }
        super.clone(tapRuntimeException);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}

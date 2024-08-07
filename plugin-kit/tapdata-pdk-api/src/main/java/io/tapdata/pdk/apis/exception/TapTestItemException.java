package io.tapdata.pdk.apis.exception;

import io.tapdata.ErrorCodeConfig;
import io.tapdata.ErrorCodeEntity;
import io.tapdata.entity.simplify.TapSimplify;
import io.tapdata.exception.TapCodeException;
import io.tapdata.exception.TapRuntimeException;
import io.tapdata.pdk.apis.exception.testItem.TapTestUnknownEx;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class TapTestItemException extends TapRuntimeException implements Serializable {
    public static final String PDK_UNKNOWN_FAIL = "pdk.unknown.fail";
    /**
     * Test item exception
     */
    private String message;
    private String reason;
    private String stack;
    private String solution;

    public TapTestItemException() {
    }

    protected TapTestItemException(Throwable cause) {
        this.stack = TapSimplify.getStackTrace(cause);
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
    protected void handleEx(Throwable cause) {
        if (null != TapSimplify.matchThrowable(cause, TapTestItemException.class)) {
            this.stack = TapSimplify.getStackTrace(cause);
        } else if (null != TapSimplify.matchThrowable(cause, TapCodeException.class)) {
            TapCodeException ex = (TapCodeException) TapSimplify.matchThrowable(cause, TapCodeException.class);
            String code = ex.getCode();
            message = ex.getMessage();
            reason = getKeyFromErrorCode(code);
            stack = TapSimplify.getStackTrace(ex);
            solution = getKeyFromErrorCode(code);
        } else {
            new TapTestUnknownEx(cause);
        }
    }
    public String getKeyFromErrorCode(String code) {
        ErrorCodeEntity errorCode = ErrorCodeConfig.getInstance().getErrorCode(code);
        if (null == errorCode) {
            return PDK_UNKNOWN_FAIL;
        }
        String solution = errorCode.getSolution();

        return solution;
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

}

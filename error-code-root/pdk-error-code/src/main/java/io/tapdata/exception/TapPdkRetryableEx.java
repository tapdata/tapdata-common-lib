package io.tapdata.exception;

import io.tapdata.PDKExCode_10;
import org.apache.commons.lang3.StringUtils;

public class TapPdkRetryableEx extends TapPdkBaseException{

    public TapPdkRetryableEx(String pdkId, Throwable cause) {
        super(PDKExCode_10.RETRYABLE_ERROR, pdkId, cause);
    }

    public TapPdkRetryableEx(String message, String pdkId, Throwable cause) {
        super(PDKExCode_10.RETRYABLE_ERROR, message, pdkId, cause);
    }

    @Override
    public String getMessage() {
        String message = "PDK retry exception (Server Error Code " + serverErrorCode + "): ";
        if (StringUtils.isNotBlank(tableName)) {
            message += String.format("when operate table: %s, ", tableName);
        }
        return message + super.getMessage();
    }
}

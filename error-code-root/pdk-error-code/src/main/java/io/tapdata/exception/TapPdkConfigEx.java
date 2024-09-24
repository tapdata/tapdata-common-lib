package io.tapdata.exception;

import io.tapdata.PDKExCode_10;

/**
 * 配置异常
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2024/9/8 19:41 Create
 */
public class TapPdkConfigEx extends TapPdkBaseException {

    public TapPdkConfigEx(String message, String pdkId) {
        super(PDKExCode_10.CONFIG_ERROR, message, pdkId);
    }

    public TapPdkConfigEx(String pdkId, Throwable cause) {
        super(PDKExCode_10.CONFIG_ERROR, pdkId, cause);
    }

    public TapPdkConfigEx(String message, String pdkId, Throwable cause) {
        super(PDKExCode_10.CONFIG_ERROR, message, pdkId, cause);
    }

    @Override
    public String getMessage() {
        return "PDK config exception (Server Error Code " + serverErrorCode + "): " + super.getMessage();
    }
}

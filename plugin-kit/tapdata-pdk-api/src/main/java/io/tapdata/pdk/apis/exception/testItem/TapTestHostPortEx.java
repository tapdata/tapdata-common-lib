package io.tapdata.pdk.apis.exception.testItem;

import io.tapdata.pdk.apis.exception.TapTestItemException;

public class TapTestHostPortEx extends TapTestItemException {
    private String host;
    private String port;
    public static final String CHECK_HOST_PORT_FAIL = "check.host.port.fail";
    public static final String CHECK_HOST_PORT_SOLUTION = "check.host.port.solution";
    public static final String CHECK_HOST_PORT_REASON = "check.host.port.reason";

    public TapTestHostPortEx(Throwable cause, String host, String port) {
        super(cause, CHECK_HOST_PORT_FAIL, CHECK_HOST_PORT_REASON, CHECK_HOST_PORT_SOLUTION);
        this.host = host;
        this.port = port;
    }

    public TapTestHostPortEx(String message, Throwable cause, String host, String port) {
        super(message, cause);
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}

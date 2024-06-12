package io.tapdata.pdk.core.error;

import io.tapdata.exception.TapCodeException;

/**
 * @author samuel
 * @Description
 * @create 2023-03-16 19:32
 **/
public class TapPdkRunnerUnknownException extends TapCodeException {
	private static final long serialVersionUID = 4820551931387403402L;
	private String tableName = "unknown";
	public TapPdkRunnerUnknownException(Throwable cause) {
		super(TapPdkRunnerExCode_18.UNKNOWN_ERROR, cause);
	}
	public String getTableName(){
		return tableName;
	}
	public void setTableName(String tableName){
		this.tableName = tableName;
	}
	@Override
	public String getMessage() {
		String message = "Unknown exception occur when operate table: %s";
		message = String.format(message, tableName);
		return message;
	}
}

package io.tapdata.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @author samuel
 * @Description
 * @create 2023-02-23 14:46
 **/
public abstract class TapPdkBaseException extends TapCodeException {
	private static final long serialVersionUID = 4820551931387403402L;
    private static final String ERROR_FMT_PDK_ID_BLANK = "Construct [%s] failed: Pdk id cannot be empty";

	protected String pdkId;
	protected String serverErrorCode;
	protected String tableName = "unknown";
	public String getTableName(){
		return tableName;
	}
	public void setTableName(String tableName){
		this.tableName = tableName;
	}

	protected TapPdkBaseException(String code, String message, String pdkId) {
		super(code, message);
		if (StringUtils.isBlank(pdkId)) {
			throw new IllegalArgumentException(String.format(ERROR_FMT_PDK_ID_BLANK, getClass().getName()));
		}
		this.pdkId = pdkId;
	}

	protected TapPdkBaseException(String code, String pdkId, Throwable cause) {
		super(code, cause);
		if (StringUtils.isBlank(pdkId)) {
			throw new IllegalArgumentException(String.format(ERROR_FMT_PDK_ID_BLANK, getClass().getName()));
		}
		this.pdkId = pdkId;
	}

	protected TapPdkBaseException(String code, String message, String pdkId, Throwable cause) {
		super(code, message, cause);
		if (StringUtils.isBlank(pdkId)) {
			throw new IllegalArgumentException(String.format(ERROR_FMT_PDK_ID_BLANK, getClass().getName()));
		}
		this.pdkId = pdkId;
	}

	public String getPdkId() {
		return pdkId;
	}

	public String getServerErrorCode() {
		return serverErrorCode;
	}

	public TapPdkBaseException withServerErrorCode(String serverErrorCode) {
		this.serverErrorCode = serverErrorCode;
		return this;
	}

	@Override
	protected void clone(TapRuntimeException tapRuntimeException) {
		if (tapRuntimeException instanceof TapPdkBaseException) {
			TapPdkBaseException tapPDKBaseException = (TapPdkBaseException) tapRuntimeException;
			tapPDKBaseException.pdkId = this.pdkId;
		}
		super.clone(tapRuntimeException);
	}
}

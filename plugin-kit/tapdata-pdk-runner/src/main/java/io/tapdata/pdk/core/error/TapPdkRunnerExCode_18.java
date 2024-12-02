package io.tapdata.pdk.core.error;

import io.tapdata.exception.TapExClass;
import io.tapdata.exception.TapExCode;

/**
 * @author samuel
 * @Description
 * @create 2023-03-16 19:31
 **/
@TapExClass(code = 18, module = "Pdk Runner", prefix = "PKR", describe = "Pdk Runner")
public interface TapPdkRunnerExCode_18 {
	@TapExCode
	String UNKNOWN_ERROR = "18001";

	@TapExCode(
			describe = "Exception occurred when calling error handling API",
			describeCN = "调用错误处理API时出现异常",
			dynamicDescription = "Current retry method name: {}, exception message: {}",
			dynamicDescriptionCN = "当前重试方法名：{}，异常信息：{}"
	)
	String CALL_ERROR_HANDLE_API_ERROR = "18002";
	@TapExCode(
			describe = "The exception class loader thrown from the PDK connector is incorrect",
			describeCN = "数据源中抛出的异常类加载器不正确",
			dynamicDescription = "Received exception class loader: {}, expected: {}",
			dynamicDescriptionCN = "接收到的类加载器类型为: {}, 期望的类加载器类型为: {}"
	)
	String EX_CLASS_LOADER_INVALID = "18003";
}

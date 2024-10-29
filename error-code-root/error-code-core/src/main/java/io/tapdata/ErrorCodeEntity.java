package io.tapdata;

import io.tapdata.exception.TapExClass;
import io.tapdata.exception.TapExLevel;
import io.tapdata.exception.TapExType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * @author samuel
 * @Description
 * @create 2023-03-16 17:47
 **/
public class ErrorCodeEntity implements Serializable {
	private static final long serialVersionUID = -6576112658078083088L;
	public static final String DEFAULT_ERROR_CODE_PREFIX = "TAP";
	private String code;
	private String name;
	private String describe;
	private String describeCN;
	private String solution;
	private String solutionCN;
	private boolean recoverable = false;
	private boolean skippable = false;
	private TapExLevel level = TapExLevel.NORMAL;
	private TapExType type = TapExType.RUNTIME;
	private Class<? extends Exception> relateException = RuntimeException.class;
	private String howToReproduce;
	private String[] seeAlso = {"https://docs.tapdata.io/"};
	private Class<?> sourceExClass;
	private String dynamicDescription;
	private String dynamicDescriptionCN;

	private ErrorCodeEntity() {
	}

	public static ErrorCodeEntity create() {
		return new ErrorCodeEntity();
	}

	public ErrorCodeEntity code(String code) {
		this.code = code;
		return this;
	}

	public ErrorCodeEntity name(String name) {
		this.name = name;
		return this;
	}

	public ErrorCodeEntity describe(String describe) {
		this.describe = describe;
		return this;
	}

	public ErrorCodeEntity describeCN(String describeCN) {
		this.describeCN = describeCN;
		return this;
	}

	public ErrorCodeEntity solution(String solution) {
		this.solution = solution;
		return this;
	}

	public ErrorCodeEntity solutionCN(String solutionCN) {
		this.solutionCN = solutionCN;
		return this;
	}

	public ErrorCodeEntity recoverable(boolean recoverable) {
		this.recoverable = recoverable;
		return this;
	}

	public ErrorCodeEntity skippable(boolean skippable) {
		this.skippable = skippable;
		return this;
	}

	public ErrorCodeEntity level(TapExLevel level) {
		this.level = level;
		return this;
	}

	public ErrorCodeEntity type(TapExType type) {
		this.type = type;
		return this;
	}

	public ErrorCodeEntity relateException(Class<? extends Exception> relateException) {
		this.relateException = relateException;
		return this;
	}

	public ErrorCodeEntity howToReproduce(String howToReproduce) {
		this.howToReproduce = howToReproduce;
		return this;
	}

	public ErrorCodeEntity seeAlso(String[] seeAlso) {
		this.seeAlso = seeAlso;
		return this;
	}

	public ErrorCodeEntity sourceExClass(Class<?> sourceExClass) {
		this.sourceExClass = sourceExClass;
		return this;
	}

	public ErrorCodeEntity dynamicDescription(String dynamicDescription) {
		this.dynamicDescription = dynamicDescription;
		return this;
	}

	public ErrorCodeEntity dynamicDescriptionCN(String dynamicDescriptionCN) {
		this.dynamicDescriptionCN = dynamicDescriptionCN;
		return this;
	}

	public String getDescribe() {
		return describe;
	}

	public String getDescribeCN() {
		return describeCN;
	}

	public String getSolution() {
		return solution;
	}

	public String getSolutionCN() {
		return solutionCN;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public boolean isSkippable() {
		return skippable;
	}

	public TapExLevel getLevel() {
		return level;
	}

	public TapExType getType() {
		return type;
	}

	public Class<? extends Exception> getRelateException() {
		return relateException;
	}

	public String getHowToReproduce() {
		return howToReproduce;
	}

	public String[] getSeeAlso() {
		return seeAlso;
	}

	public Class<?> getSourceExClass() {
		return sourceExClass;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDynamicDescription() {
		return dynamicDescription;
	}

	public String getDynamicDescriptionCN() {
		return dynamicDescriptionCN;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ErrorCodeEntity.class.getSimpleName() + "[", "]")
				.add("code='" + code + "'")
				.add("name='" + name + "'")
				.add("describe='" + describe + "'")
				.add("describeCN='" + describeCN + "'")
				.add("solution='" + solution + "'")
				.add("solutionCN='" + solutionCN + "'")
				.add("recoverable=" + recoverable)
				.add("skippable=" + skippable)
				.add("level=" + level)
				.add("type=" + type)
				.add("relateException=" + relateException)
				.add("howToReproduce='" + howToReproduce + "'")
				.add("seeAlso=" + Arrays.toString(seeAlso))
				.add("sourceExClass='" + sourceExClass + "'")
				.add("dynamicDescription='" + dynamicDescription + "'")
				.add("dynamicDescriptionCN='" + dynamicDescription + "'")
				.toString();
	}

	public String fullErrorCode() {
		String code = this.code;
		String prefix = DEFAULT_ERROR_CODE_PREFIX;
		if (StringUtils.isBlank(code)) {
			return "";
		}
		if (null != this.sourceExClass) {
			TapExClass tapExClass = this.sourceExClass.getAnnotation(TapExClass.class);
			String codePrefix = tapExClass.prefix();
			if (StringUtils.isNotBlank(codePrefix)) {
				prefix = codePrefix;
			}
		}
		prefix = prefix.toUpperCase();
		return prefix + code;
	}
}

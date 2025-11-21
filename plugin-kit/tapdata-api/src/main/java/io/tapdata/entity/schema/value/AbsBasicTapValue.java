package io.tapdata.entity.schema.value;

import io.tapdata.entity.schema.type.TapType;

import java.io.Serializable;

/**
 * 基础类型处理基类
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2025/11/19 15:46 Create
 */
public abstract class AbsBasicTapValue<T extends Serializable, P extends TapType> extends TapValue<T, P> implements Serializable {
}

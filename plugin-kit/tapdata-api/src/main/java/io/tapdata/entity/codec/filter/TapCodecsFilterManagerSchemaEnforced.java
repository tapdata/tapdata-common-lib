package io.tapdata.entity.codec.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.tapdata.entity.codec.FromTapValueCodec;
import io.tapdata.entity.codec.TapCodecsRegistry;
import io.tapdata.entity.codec.TapDefaultCodecs;
import io.tapdata.entity.codec.ToTapValueCodec;
import io.tapdata.entity.codec.detector.TapDetector;
import io.tapdata.entity.codec.detector.TapSkipper;
import io.tapdata.entity.codec.filter.entity.TransformToTapValueFieldWrapper;
import io.tapdata.entity.codec.filter.exception.TapCodecsFilterManagerException;
import io.tapdata.entity.error.UnknownCodecException;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.schema.TapField;
import io.tapdata.entity.schema.TapTable;
import io.tapdata.entity.schema.type.TapType;
import io.tapdata.entity.schema.value.TapValue;
import io.tapdata.entity.utils.InstanceFactory;
import io.tapdata.entity.utils.JavaTypesToTapTypes;
import io.tapdata.entity.utils.PropertyUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.tapdata.entity.simplify.TapSimplify.field;

/**
 * @author samuel
 * @Description
 * @create 2024-07-11 15:24
 **/
public class TapCodecsFilterManagerSchemaEnforced extends TapCodecsFilterManager {
	private static final String TAG = TapCodecsFilterManagerSchemaEnforced.class.getSimpleName();
	public static final String TAPDATA_TRANSFORM_TABLE_FIELD_CACHE_MAXSIZE_PROP_KEY = "TAPDATA_TRANSFORM_TABLE_FIELD_CACHE_MAXSIZE";

	private final Cache<String, TransformToTapValueFieldWrapper> transformToTapValueInsertFieldWrapperCache;

	public TapCodecsFilterManagerSchemaEnforced(TapCodecsRegistry codecsRegistry) {
		super(codecsRegistry);
		int tableFieldCacheMaxSize = PropertyUtils.getPropertyInt(TAPDATA_TRANSFORM_TABLE_FIELD_CACHE_MAXSIZE_PROP_KEY, 5);
		this.transformToTapValueInsertFieldWrapperCache = CacheBuilder.newBuilder()
				.maximumSize(tableFieldCacheMaxSize)
				.expireAfterAccess(5L, TimeUnit.MINUTES)
				.build();
	}
	public Set<String> transformToTapValueMap(Map<String, Object> data, TapTable tapTable, TapDetector... detectors) {
		return transformToTapValueMap(data, tapTable, null, detectors);
	}

	public Set<String> transformToTapValueMap(Map<String, Object> data, TapTable tapTable, Map<String, TapValue<?, ?>> originValueMap, TapDetector... detectors) {
		if (data == null)
			return new HashSet<>();
		Set<String> transformedToTapValueFieldNames = new HashSet<>();
		AtomicReference<ToTapValueCheck> toTapValueCheckRef = new AtomicReference<>();
		AtomicReference<TapSkipper> skipperRef = new AtomicReference<>();
		if (detectors != null) {
			for (TapDetector detector : detectors) {
				if (toTapValueCheckRef.get() == null && detector instanceof ToTapValueCheck) {
					toTapValueCheckRef.set((ToTapValueCheck) detector);
				} else if (skipperRef.get() == null && detector instanceof TapSkipper) {
					skipperRef.set((TapSkipper) detector);
				}
			}
		}
		TransformToTapValueFieldWrapper transformToTapValueFieldWrapper;
		try {
			transformToTapValueFieldWrapper = getOrInitTransformToTapValueFieldWrapper(data, tapTable);
		} catch (ExecutionException e) {
			throw new TapCodecsFilterManagerException("Failed to get or init TransformToTapValueFieldWrapper, table id: " + tapTable.getId(), e);
		}
		AtomicInteger index = new AtomicInteger(0);
		mapIteratorToTapValue.iterate(data, (key, value, recursive) -> {
			String fieldName = key;
			if (recursive) {
				fieldName = fieldName(key);
			}
			TapValue<?, ?> originTapValue = null;
			TapField field = transformToTapValueFieldWrapper.getField(index.getAndIncrement());
			if (value != null && fieldName != null) {
				if ((value instanceof TapValue)) {
					TapLogger.debug(TAG, "Value {} for field {} already in TapValue format, no need do ToTapValue conversion. ", value, fieldName);
					return null;
				}

				String dataType = null;
				TapType typeFromSchema = null;
				ToTapValueCodec<?> valueCodec = null;

				boolean newField = false;

				originTapValue = originValueMap != null ? originValueMap.get(fieldName) : null;

				if (transformToTapValueFieldWrapper.getTapFieldList() != null) {
					if (null != skipperRef.get() && skipperRef.get().skip(field)) {
						return null;
					}
					valueCodec = this.codecsRegistry.getCustomToTapValueCodec(value.getClass());

					if (field != null) {
						dataType = field.getDataType();
						typeFromSchema = field.getTapType();
						if (typeFromSchema != null && valueCodec == null) {
							valueCodec = getValueCodec(typeFromSchema);
							boolean isTypeQualified = true;
							switch (typeFromSchema.getType()) {
								case TapType.TYPE_ARRAY:
									if (!(value instanceof Collection)) isTypeQualified = false;
									break;
								case TapType.TYPE_MAP:
									if (!(value instanceof Map)) isTypeQualified = false;
									break;
								case TapType.TYPE_STRING:
									if (!(value instanceof String)) isTypeQualified = false;
									break;
								case TapType.TYPE_NUMBER:
									if (!(value instanceof Number)) isTypeQualified = false;
									break;
								case TapType.TYPE_BINARY:
									if (!(value instanceof byte[])) isTypeQualified = false;
									break;
								default:
									break;
							}
							if (!isTypeQualified) {
								valueCodec = null;
								newField = true;
							}
						} else {
							newField = true;
						}
					} else {
						newField = true;
					}
				}

				if (newField && valueCodec == null) {
					valueCodec = getTapValueCodec(value);
					typeFromSchema = JavaTypesToTapTypes.toTapType(value);
				}
				if (valueCodec != null) {
					TapValue tapValue = valueCodec.toTapValue(value, typeFromSchema);
					if (tapValue == null && !newField) {
						TapLogger.debug(TAG, "Value Codec {} from model convert TapValue failed, value {}", valueCodec.getClass().getSimpleName(), value);
						valueCodec = getTapValueCodec(value);
						if (valueCodec != null) {
							tapValue = valueCodec.toTapValue(value, typeFromSchema);
							if (tapValue == null) {
								TapLogger.debug(TAG, "Value Codec {} from type convert TapValue failed, value {}", valueCodec.getClass().getSimpleName(), value);
							} else {
								if (typeFromSchema != null && !typeFromSchema.getClass().equals(tapValue.tapTypeClass())) {
									typeFromSchema = JavaTypesToTapTypes.toTapType(value);
								}
							}
						}
					}
					if (tapValue == null) {
						tapValue = InstanceFactory.instance(ToTapValueCodec.class, TapDefaultCodecs.TAP_RAW_VALUE)
								.toTapValue(value, typeFromSchema);
					}
					if (typeFromSchema == null)
						typeFromSchema = tapValue.createDefaultTapType();
					//noinspection unchecked
					tapValue.setTapType(typeFromSchema);
					if (!value.equals(tapValue.getValue())) {
						tapValue.setOriginValue(value);
					}
					tapValue.setOriginType(dataType);

					if (originTapValue != null) {
						if (originTapValue.getValue().equals(tapValue.getValue())) {
							if (tapValue.getOriginValue() == null) {
								tapValue.setOriginValue(originTapValue.getOriginValue());
								tapValue.setOriginType(originTapValue.getOriginType());
							}
						}
					}
					if (toTapValueCheckRef.get() == null) {
						transformedToTapValueFieldNames.add(fieldName);
						return tapValue;
					} else if (!toTapValueCheckRef.get().check(fieldName, tapValue.getValue())) {
						throw new StopFilterException();
					}
					return null;
				}
			}
			if (originTapValue != null && originTapValue.getValue().equals(value)) {
				if (toTapValueCheckRef.get() == null)
					return originTapValue;
				else if (!toTapValueCheckRef.get().check(fieldName, originTapValue.getValue()))
					throw new StopFilterException();
				return null;
			}
			if (toTapValueCheckRef.get() == null)
				return value;
			else if (!toTapValueCheckRef.get().check(fieldName, value))
				throw new StopFilterException();

			return null;
		});
		return transformedToTapValueFieldNames;
	}

	private TransformToTapValueFieldWrapper getOrInitTransformToTapValueFieldWrapper(Map<String, Object> value, TapTable tapTable) throws ExecutionException {
		return transformToTapValueInsertFieldWrapperCache.get(tapTable.getId(), () -> {
			LinkedHashMap<String, TapField> nameFieldMap = tapTable.getNameFieldMap();
			TransformToTapValueFieldWrapper result = TransformToTapValueFieldWrapper.create(tapTable.getId());
			mapIteratorToTapValue.iterate(value, (key, value1, recursive) -> {
				String fieldName = key;
				if (recursive) {
					fieldName = fieldName(key);
				}
				TapField tapField = nameFieldMap.get(fieldName);
				result.addField(tapField);
				return null;
			});
			return result;
		});
	}

	public void transformFromTapValueMap(
			Map<String, Object> tapValueMap,
			Map<String, TapField> sourceNameFieldMap,
			Set<String> transformedToTapValueFieldNames
	) {
		if (null == transformedToTapValueFieldNames || transformedToTapValueFieldNames.isEmpty()) {
			return;
		}
		for (String transformedToTapValueFieldName : transformedToTapValueFieldNames) {
			Object object = tapValueMap.get(transformedToTapValueFieldName);
			String fieldName = transformedToTapValueFieldName;
			if (object instanceof TapValue) {
				TapValue<?, ?> theValue = (TapValue<?, ?>) object;
				if (transformedToTapValueFieldName != null && null != theValue) {
					FromTapValueCodec<TapValue<?, ?>> fromTapValueCodec = this.codecsRegistry.getCustomFromTapValueCodec((Class<TapValue<?, ?>>) theValue.getClass());
					if (fromTapValueCodec == null) {
						fromTapValueCodec = this.codecsRegistry.getDefaultFromTapValueCodec((Class<TapValue<?, ?>>) theValue.getClass());
					}
					if (fromTapValueCodec == null)
						throw new UnknownCodecException("fromTapValueMap codecs not found for value class " + theValue.getClass());
					Object value = fromTapValueCodec.fromTapValue(theValue);
					tapValueMap.put(fieldName, value);
				}
			}
		}
	}
}

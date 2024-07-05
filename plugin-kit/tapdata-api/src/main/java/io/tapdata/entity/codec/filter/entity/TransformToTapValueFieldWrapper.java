package io.tapdata.entity.codec.filter.entity;

import io.tapdata.entity.schema.TapField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author samuel
 * @Description
 * @create 2024-07-05 16:33
 **/
public class TransformToTapValueFieldWrapper {
	private final String tableId;
	private final List<TapField> tapFieldList;

	private TransformToTapValueFieldWrapper(String tableId) {
		this.tableId = tableId;
		this.tapFieldList = new ArrayList<>();
	}

	public static TransformToTapValueFieldWrapper create(String tableId) {
		return new TransformToTapValueFieldWrapper(tableId);
	}

	public void addField(TapField tapField) {
		tapFieldList.add(tapField);
	}

	public String getTableId() {
		return tableId;
	}

	public TapField getField(int index) {
		if(index < 0 || index >= tapFieldList.size()) {
			return null;
		}
		return tapFieldList.get(index);
	}

	public List<TapField> getTapFieldList() {
		return tapFieldList;
	}
}

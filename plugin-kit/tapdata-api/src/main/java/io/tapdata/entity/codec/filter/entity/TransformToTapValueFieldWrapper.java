package io.tapdata.entity.codec.filter.entity;

import io.tapdata.entity.codec.ToTapValueCodec;
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
	private final List<FieldPlan> fieldPlanList;

	private TransformToTapValueFieldWrapper(String tableId) {
		this.tableId = tableId;
		this.tapFieldList = new ArrayList<>();
		this.fieldPlanList = new ArrayList<>();
	}

	public static TransformToTapValueFieldWrapper create(String tableId) {
		return new TransformToTapValueFieldWrapper(tableId);
	}

	public void addField(TapField tapField) {
		tapFieldList.add(tapField);
		fieldPlanList.add(new FieldPlan(tapField, null));
	}

	public void addField(TapField tapField, ToTapValueCodec<?> schemaCodec) {
		tapFieldList.add(tapField);
		fieldPlanList.add(new FieldPlan(tapField, schemaCodec));
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

	public FieldPlan getFieldPlan(int index) {
		if(index < 0 || index >= fieldPlanList.size()) {
			return null;
		}
		return fieldPlanList.get(index);
	}

	public static class FieldPlan {
		private final TapField tapField;
		private final ToTapValueCodec<?> schemaCodec;

		private FieldPlan(TapField tapField, ToTapValueCodec<?> schemaCodec) {
			this.tapField = tapField;
			this.schemaCodec = schemaCodec;
		}

		public TapField getTapField() {
			return tapField;
		}

		public ToTapValueCodec<?> getSchemaCodec() {
			return schemaCodec;
		}
	}
}

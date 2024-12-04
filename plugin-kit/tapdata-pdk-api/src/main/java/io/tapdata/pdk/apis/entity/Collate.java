package io.tapdata.pdk.apis.entity;

public class Collate {
    private String fieldName;
    private String collateName;

    public Collate() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getCollateName() {
        return collateName;
    }

    public void setCollateName(String collateName) {
        this.collateName = collateName;
    }

    public Collate(String fieldName, String collateName) {
        this.fieldName = fieldName;
        this.collateName = collateName;
    }
}

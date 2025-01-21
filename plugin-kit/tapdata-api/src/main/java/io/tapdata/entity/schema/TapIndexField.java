package io.tapdata.entity.schema;

import java.io.Serializable;
import java.util.List;

public class TapIndexField implements Serializable {
    /**
     * Index name
     */
    private String name;
    public TapIndexField name(String name) {
        this.name = name;
        return this;
    }
    /**
     * Field is asc or not.
     */
    private Boolean fieldAsc;
    public TapIndexField fieldAsc(Boolean fieldAsc) {
        this.fieldAsc = fieldAsc;
        return this;
    }

    private String type;
    public TapIndexField fieldType(String type) {
        this.type = type;
        return this;
    }

    private Integer subPart;

    public Integer getSubPart() {
        return subPart;
    }

    public void setSubPart(Integer subPart) {
        this.subPart = subPart;
    }

    public Boolean getFieldAsc() {
        return fieldAsc;
    }

    public void setFieldAsc(Boolean fieldAsc) {
        this.fieldAsc = fieldAsc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TapIndexField name " + name + " fieldAsc " + fieldAsc + " indexType " + type + " subPart " + subPart;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

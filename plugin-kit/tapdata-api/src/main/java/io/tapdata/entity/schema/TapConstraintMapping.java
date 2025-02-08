package io.tapdata.entity.schema;

import java.io.Serializable;

public class TapConstraintMapping implements Serializable {

    private String foreignKey;
    private String referenceKey;

    public TapConstraintMapping foreignKey(String foreignKey) {
        this.foreignKey = foreignKey;
        return this;
    }

    public TapConstraintMapping referenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
        return this;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getReferenceKey() {
        return referenceKey;
    }

    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }
}

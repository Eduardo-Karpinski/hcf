package com.hcf;

import java.util.Objects;

public class HCFJoinSearch {

    private String primaryField;
    private String foreignField;
    private Object value;
    private Class<?> joinClass;

    public HCFJoinSearch() {
    }

    public String getPrimaryField() {
        return primaryField;
    }

    public HCFJoinSearch setPrimaryField(String primaryField) {
        this.primaryField = primaryField;
        return this;
    }

    public String getForeignField() {
        return foreignField;
    }

    public HCFJoinSearch setForeignField(String foreignField) {
        this.foreignField = foreignField;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public HCFJoinSearch setValue(Object value) {
        this.value = value;
        return this;
    }

    public Class<?> getJoinClass() {
        return joinClass;
    }

    public HCFJoinSearch setJoinClass(Class<?> joinClass) {
        this.joinClass = joinClass;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreignField, joinClass, primaryField, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HCFJoinSearch other = (HCFJoinSearch) obj;
        return Objects.equals(foreignField, other.foreignField) && Objects.equals(joinClass, other.joinClass)
                && Objects.equals(primaryField, other.primaryField) && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "HCFJoinSearch [primaryField=" + primaryField + ", foreignField=" + foreignField + ", value=" + value
                + ", joinClass=" + joinClass + "]";
    }

}
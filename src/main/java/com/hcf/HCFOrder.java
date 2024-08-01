package com.hcf;

import java.util.Objects;

public class HCFOrder {

    private Boolean asc;
    private String field;
    private Integer limit;
    private Integer offset;

    public HCFOrder() {

    }

    public HCFOrder(Boolean asc, String field, Integer limit, Integer offset) {
        this.asc = asc;
        this.field = field;
        this.limit = limit;
        this.offset = offset;
    }

    public Boolean getAsc() {
        return asc;
    }

    public HCFOrder setAsc(Boolean asc) {
        this.asc = asc;
        return this;
    }

    public String getField() {
        return field;
    }

    public HCFOrder setField(String field) {
        this.field = field;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public HCFOrder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public HCFOrder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asc, field, limit, offset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HCFOrder other = (HCFOrder) obj;
        return Objects.equals(asc, other.asc) && Objects.equals(field, other.field)
                && Objects.equals(limit, other.limit) && Objects.equals(offset, other.offset);
    }

    @Override
    public String toString() {
        return "HCFOrder [asc=" + asc + ", field=" + field + ", limit=" + limit + ", offset=" + offset + "]";
    }

}
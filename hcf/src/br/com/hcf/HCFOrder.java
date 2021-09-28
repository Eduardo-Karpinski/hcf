package br.com.hcf;

public class HCFOrder {

	private Boolean asc;
	private String field;
	private Integer limit;
	private Integer offset;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asc == null) ? 0 : asc.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((limit == null) ? 0 : limit.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		return result;
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
		if (asc == null) {
			if (other.asc != null)
				return false;
		} else if (!asc.equals(other.asc))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (limit == null) {
			if (other.limit != null)
				return false;
		} else if (!limit.equals(other.limit))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HCFOrder [asc=" + asc + ", field=" + field + ", limit=" + limit + ", offset=" + offset + "]";
	}

}
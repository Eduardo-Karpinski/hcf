package br.com.hcf;

public final class HCFOrder {

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

	public void setAsc(Boolean asc) {
		this.asc = asc;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

}

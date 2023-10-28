package com.hcf;

import java.util.Objects;

import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;

public class HCFSearch {

	private String field;
	private Object value;
	private HCFParameter parameter;
	private HCFOperator operator;

	public HCFSearch() {
		// empty constructor created to give more options when instantiating the class
	}

	public HCFSearch(String field, Object value, HCFParameter parameter, HCFOperator operator) {
		this.field = field;
		this.value = value;
		this.parameter = parameter;
		this.operator = operator;
	}

	public String getField() {
		return field;
	}

	public HCFSearch setField(String field) {
		this.field = field;
		return this;
	}

	public Object getValue() {
		return value;
	}

	public HCFSearch setValue(Object value) {
		this.value = value;
		return this;
	}

	public HCFParameter getParameter() {
		return parameter;
	}

	public HCFSearch setParameter(HCFParameter parameter) {
		this.parameter = parameter;
		return this;
	}

	public HCFOperator getOperator() {
		return operator;
	}

	public HCFSearch setOperator(HCFOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, operator, parameter, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HCFSearch other = (HCFSearch) obj;
		return Objects.equals(field, other.field) && operator == other.operator && parameter == other.parameter
				&& Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "HCFSearch [field=" + field + ", value=" + value + ", parameter=" + parameter + ", operator=" + operator
				+ "]";
	}

}
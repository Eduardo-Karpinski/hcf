package br.com.hcf;

import br.com.hcf.enums.HCFOperator;
import br.com.hcf.enums.HCFParameter;

public class HCFSearch {
	
	private String field;
	private Object value;
	private HCFParameter parameter;
	private HCFOperator operator;
	
	public HCFSearch() {

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		HCFSearch other = (HCFSearch) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (operator != other.operator)
			return false;
		if (parameter != other.parameter)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HCFSearch [field=" + field + ", value=" + value + ", parameter=" + parameter + ", operator=" + operator
				+ "]";
	}

}
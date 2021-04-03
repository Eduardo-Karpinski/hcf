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

}

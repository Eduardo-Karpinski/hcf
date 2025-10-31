package com.hcf.query;

import com.hcf.query.enums.HCFOperator;
import com.hcf.query.enums.HCFParameter;

public record HCFSearch(String field, Object value, HCFParameter parameter, HCFOperator operator) {

}
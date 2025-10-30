package com.hcf.query;

import java.util.Objects;

public record HCFSort(String field, boolean asc) {
	public HCFSort {
		Objects.requireNonNull(field, "field is null");
		if (field.isBlank()) {
			throw new IllegalArgumentException("field is blank");
		}
	}
}
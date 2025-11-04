package com.hcf.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hcf.query.HCFSearch;
import com.hcf.query.enums.HCFOperator;
import com.hcf.query.enums.HCFParameter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class HCFPredicateUtil {
	
	private HCFPredicateUtil() {}

	public static Path<?> resolvePath(Root<?> root, String fieldPath) {
		Objects.requireNonNull(root, "root is null");
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		String[] parts = fieldPath.split("\\.");
		Path<?> path = root.get(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			path = path.get(parts[i]);
		}
		return path;
	}

	public static Path<?> resolvePath(Root<?> root, Map<String, From<?, ?>> joins, String fieldPath) {
		Objects.requireNonNull(root, "root is null");
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		if (joins == null || joins.isEmpty()) {
			return resolvePath(root, fieldPath);
		}

		String[] parts = fieldPath.split("\\.");
		From<?, ?> from = null;
		int start = 0;

		if (joins.containsKey(fieldPath)) {
			from = joins.get(fieldPath);
			start = parts.length;
		} else {
			String prefix = "";
			for (int i = 0; i < parts.length; i++) {
				prefix = (i == 0) ? parts[0] : prefix + "." + parts[i];
				if (joins.containsKey(prefix)) {
					from = joins.get(prefix);
					start = i + 1;
				}
			}
		}

		Path<?> path;
		if (from == null) {
			path = root.get(parts[0]);
			start = 1;
		} else {
			path = from;
		}

		for (int i = start; i < parts.length; i++) {
			path = path.get(parts[i]);
		}
		return path;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Predicate singlePredicate(CriteriaBuilder criteriaBuilder, Path<?> field, HCFParameter parameter, Object value) {
		return switch (parameter) {
		case TRUE -> criteriaBuilder.isTrue(field.as(Boolean.class));
		case FALSE -> criteriaBuilder.isFalse(field.as(Boolean.class));
		case IS_NULL -> criteriaBuilder.isNull(field);
		case IS_NOT_NULL -> criteriaBuilder.isNotNull(field);
		case EMPTY -> criteriaBuilder.equal(criteriaBuilder.length(criteriaBuilder.trim(field.as(String.class))), 0);
		case NOT_EMPTY -> criteriaBuilder.notEqual(criteriaBuilder.length(criteriaBuilder.trim(field.as(String.class))), 0);
		case IS_ODD -> criteriaBuilder.equal(criteriaBuilder.mod(field.as(Integer.class), 2), 1);
		case IS_EVEN -> criteriaBuilder.equal(criteriaBuilder.mod(field.as(Integer.class), 2), 0);
		case LIKE -> criteriaBuilder.like(field.as(String.class), Objects.toString(value, ""));
		case NOT_LIKE -> criteriaBuilder.notLike(field.as(String.class), Objects.toString(value, ""));
		case EQUAL -> criteriaBuilder.equal(field, value);
		case NOT_EQUAL -> criteriaBuilder.notEqual(field, value);
		case LESS_THAN -> criteriaBuilder.lessThan((Expression) field, (Comparable) value);
		case GREATER_THAN -> criteriaBuilder.greaterThan((Expression) field, (Comparable) value);
		case LESS_THAN_OR_EQUAL_TO -> criteriaBuilder.lessThanOrEqualTo((Expression) field, (Comparable) value);
		case GREATER_THAN_OR_EQUAL_TO -> criteriaBuilder.greaterThanOrEqualTo((Expression) field, (Comparable) value);
		};
	}

	public static Predicate buildForRoot(CriteriaBuilder cb, Root<?> root, List<HCFSearch> params) {
		if (params == null || params.isEmpty())
			return null;

		Deque<Predicate> stack = new ArrayDeque<Predicate>();
		for (int i = 0; i < params.size(); i++) {
			HCFSearch search = params.get(i);
			Path<?> field = resolvePath(root, search.field());
			Predicate predicate = singlePredicate(cb, field, search.parameter(), search.value());
			HCFOperator operator = search.operator();

			if (operator == HCFOperator.NONE) {
				stack.addLast(predicate);
			} else {
				if (stack.isEmpty()) {
					stack.addLast(predicate);
				} else {
					Predicate left = stack.removeLast();
					stack.addLast(operator == HCFOperator.AND ? cb.and(left, predicate) : cb.or(left, predicate));
				}
			}
		}
		return stack.getLast();
	}

	public static Predicate buildForJoins(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins, List<HCFSearch> params) {
		if (params == null || params.isEmpty()) {
			return null;
		}

		Deque<Predicate> stack = new ArrayDeque<Predicate>();
		for (int i = 0; i < params.size(); i++) {
			HCFSearch search = params.get(i);
			Path<?> field = resolvePath(root, joins, search.field());
			Predicate predicate = singlePredicate(cb, field, search.parameter(), search.value());
			HCFOperator operator = search.operator();

			if (operator == HCFOperator.NONE) {
				stack.addLast(predicate);
			} else {
				if (stack.isEmpty()) {
					stack.addLast(predicate);
				} else {
					Predicate left = stack.removeLast();
					stack.addLast(operator == HCFOperator.AND ? cb.and(left, predicate) : cb.or(left, predicate));
				}
			}
		}
		return stack.getLast();
	}
	
}
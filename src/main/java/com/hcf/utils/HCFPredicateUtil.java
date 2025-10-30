package com.hcf.utils;

import java.util.ArrayDeque;
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
		Path<?> p = root.get(parts[0]);
		for (int i = 1; i < parts.length; i++)
			p = p.get(parts[i]);
		return p;
	}

	public static Path<?> resolvePath(Root<?> root, Map<String, From<?, ?>> joins, String fieldPath) {
		Objects.requireNonNull(root, "root is null");
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		if (joins == null || joins.isEmpty()) {
			return resolvePath(root, fieldPath);
		}

		String[] parts = fieldPath.split("\\.");
		From<?, ?> base = null;
		int start = 0;

		if (joins.containsKey(fieldPath)) {
			base = joins.get(fieldPath);
			start = parts.length;
		} else {
			String prefix = "";
			for (int i = 0; i < parts.length; i++) {
				prefix = (i == 0) ? parts[0] : prefix + "." + parts[i];
				if (joins.containsKey(prefix)) {
					base = joins.get(prefix);
					start = i + 1;
				}
			}
		}

		Path<?> path;
		if (base == null) {
			path = root.get(parts[0]);
			start = 1;
		} else {
			path = base;
		}

		for (int i = start; i < parts.length; i++)
			path = path.get(parts[i]);
		return path;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Predicate singlePredicate(CriteriaBuilder cb, Path<?> field, HCFParameter p, Object v) {
		return switch (p) {
		case TRUE -> cb.isTrue(field.as(Boolean.class));
		case FALSE -> cb.isFalse(field.as(Boolean.class));
		case IS_NULL -> cb.isNull(field);
		case IS_NOT_NULL -> cb.isNotNull(field);
		case EMPTY -> cb.equal(cb.length(cb.trim(field.as(String.class))), 0);
		case NOT_EMPTY -> cb.notEqual(cb.length(cb.trim(field.as(String.class))), 0);
		case IS_ODD -> cb.equal(cb.mod(field.as(Integer.class), 2), 1);
		case IS_EVEN -> cb.equal(cb.mod(field.as(Integer.class), 2), 0);
		case LIKE -> cb.like(field.as(String.class), Objects.toString(v, ""));
		case NOT_LIKE -> cb.notLike(field.as(String.class), Objects.toString(v, ""));
		case EQUAL -> cb.equal(field, v);
		case NOT_EQUAL -> cb.notEqual(field, v);
		case LESS_THAN -> cb.lessThan((Expression) field, (Comparable) v);
		case GREATER_THAN -> cb.greaterThan((Expression) field, (Comparable) v);
		case LESS_THAN_OR_EQUAL_TO -> cb.lessThanOrEqualTo((Expression) field, (Comparable) v);
		case GREATER_THAN_OR_EQUAL_TO -> cb.greaterThanOrEqualTo((Expression) field, (Comparable) v);
		};
	}

	public static Predicate buildForRoot(CriteriaBuilder cb, Root<?> root, List<HCFSearch> params) {
		if (params == null || params.isEmpty())
			return null;

		var stack = new ArrayDeque<Predicate>();
		for (int i = 0; i < params.size(); i++) {
			var s = params.get(i);
			Path<?> field = resolvePath(root, s.getField());
			Predicate p = singlePredicate(cb, field, s.getParameter(), s.getValue());
			HCFOperator op = s.getOperator();

			if (op == HCFOperator.NONE) {
				stack.addLast(p);
			} else {
				if (stack.isEmpty()) {
					stack.addLast(p);
				} else {
					Predicate left = stack.removeLast();
					stack.addLast(op == HCFOperator.AND ? cb.and(left, p) : cb.or(left, p));
				}
			}
		}
		return stack.getLast();
	}

	public static Predicate buildForJoins(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins, List<HCFSearch> params) {
		if (params == null || params.isEmpty())
			return null;

		var stack = new ArrayDeque<Predicate>();
		for (int i = 0; i < params.size(); i++) {
			var s = params.get(i);
			Path<?> field = resolvePath(root, joins, s.getField());
			Predicate p = singlePredicate(cb, field, s.getParameter(), s.getValue());
			HCFOperator op = s.getOperator();

			if (op == HCFOperator.NONE) {
				stack.addLast(p);
			} else {
				if (stack.isEmpty()) {
					stack.addLast(p);
				} else {
					Predicate left = stack.removeLast();
					stack.addLast(op == HCFOperator.AND ? cb.and(left, p) : cb.or(left, p));
				}
			}
		}
		return stack.getLast();
	}
	
}
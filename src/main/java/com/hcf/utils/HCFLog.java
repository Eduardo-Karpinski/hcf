package com.hcf.utils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HCFLog {
	
	private static final Logger logger = HCFUtil.getLogger();

	public static String showError(Throwable t) {
		return showError(t, null);
	}

	public static String showError(Throwable t, String context) {
		if (t == null) {
			return null;
		}

		String id = UUID.randomUUID().toString();
		String thread = Thread.currentThread().getName();
		String type = t.getClass().getSimpleName();
		String msg = String.valueOf(t.getMessage());

		String summary = String.format("[HCF-ERROR] id=%s thread=%s type=%s msg=%s%s", id, thread, type, msg, (context == null || context.isBlank() ? "" : " ctx=" + context));
		logger.severe(summary);

		Throwable root = t;
		StringBuilder chain = new StringBuilder("[HCF-ERROR] cause-chain: ");
		chain.append(type).append(": ").append(msg);
		while (root.getCause() != null) {
			root = root.getCause();
			chain
			.append(" -> ")
			.append(root.getClass().getSimpleName())
			.append(": ")
			.append(String.valueOf(root.getMessage()));
		}
		logger.severe(chain.toString());

		for (Throwable sup : t.getSuppressed()) {
			logger.severe("[HCF-ERROR] suppressed: " + sup.getClass().getSimpleName() + ": " + sup.getMessage());
		}

		if (t instanceof SQLException) {
			logSqlExceptionChain((SQLException) t);
		} else if (root instanceof SQLException) {
			logSqlExceptionChain((SQLException) root);
		}

		logger.log(Level.SEVERE, "[HCF-ERROR] full-stack id=" + id, t);

		return id;
	}

	private static void logSqlExceptionChain(SQLException ex) {
		int i = 0;
		for (SQLException e = ex; e != null; e = e.getNextException()) {
			logger.severe(String.format("[HCF-ERROR][SQL][%d] SQLState=%s vendorCode=%d msg=%s", i++, e.getSQLState(), e.getErrorCode(), String.valueOf(e.getMessage())));
		}
	}

	private HCFLog() {
	}
}

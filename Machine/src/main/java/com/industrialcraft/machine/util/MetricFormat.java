package com.industrialcraft.machine.util;

import java.util.Locale;

public final class MetricFormat {
	private static final String[] PREFIXES = {"", "k", "M", "G", "T", "P", "E"};

	private MetricFormat() {
	}

	public static String formatWithUnit(double value, String unit) {
		if (!Double.isFinite(value) || value == 0.0D) {
			return String.format(Locale.ROOT, "%.2f %s", 0.0D, unit);
		}

		double scaled = value;
		int prefixIndex = 0;
		double abs = Math.abs(scaled);
		while (abs >= 1000.0D && prefixIndex < PREFIXES.length - 1) {
			scaled /= 1000.0D;
			abs /= 1000.0D;
			prefixIndex++;
		}

		return String.format(Locale.ROOT, "%.2f %s%s", scaled, PREFIXES[prefixIndex], unit);
	}
}

package io.github.lounode.extrabotany.common.item;

import java.time.LocalDate;
import java.time.Month;

public final class SeasonalItemHelper {
	private SeasonalItemHelper() {}

	public static boolean isChristmas() {
		LocalDate now = LocalDate.now();
		return now.getMonth() == Month.DECEMBER && now.getDayOfMonth() >= 16
				|| now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 2;
	}
}

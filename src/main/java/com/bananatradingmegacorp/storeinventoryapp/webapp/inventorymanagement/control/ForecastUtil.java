package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import java.time.Instant;

public final class ForecastUtil {
    private ForecastUtil() {
    }

    public static void checkForecastIsInFuture(
            final Instant placementTimestamp,
            final Instant forecastTimestamp
    ) {
        if (placementTimestamp.compareTo(forecastTimestamp) > 0) {
            throw new RuntimeException(
                    "forecast timestamp (%s) is behind the placement timestamp (%s)".formatted(forecastTimestamp, placementTimestamp)
            );
        }
    }
}

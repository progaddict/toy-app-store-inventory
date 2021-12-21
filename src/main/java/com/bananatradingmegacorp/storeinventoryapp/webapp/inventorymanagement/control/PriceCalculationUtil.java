package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

public final class PriceCalculationUtil {
    private PriceCalculationUtil() {
    }

    public static int getGeneralDailyPriceInCents(
            final int basePriceInCents,
            final int quality
    ) {
        return basePriceInCents + 10 * quality;
    }
}

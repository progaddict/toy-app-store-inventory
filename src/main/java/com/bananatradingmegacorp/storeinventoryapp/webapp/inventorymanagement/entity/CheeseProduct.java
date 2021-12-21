package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control.ForecastUtil;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control.PriceCalculationUtil;

import java.time.Duration;
import java.time.Instant;

public class CheeseProduct extends AbstractProduct {
    public static final String NAME = "cheese";

    private static final Duration FIFTY_DAYS = Duration.ofDays(50);
    private static final Duration HUNDRED_DAYS = Duration.ofDays(100);
    private static final int MIN_QUALITY = 30;

    public CheeseProduct(
            final BasicProductInformationModel basicProductInformation,
            final Instant placementTimestamp
    ) {
        super(basicProductInformation, placementTimestamp);
    }

    @Override
    public ForecastedProductInformationModel getForecastedProductInformationModel(final Instant timestamp) {
        final ForecastedProductInformationModel.ForecastedProductInformationModelBuilder b
                = ForecastedProductInformationModel.buildFrom(getBasicProductInformation())
                // at the beginning, suppose, it must be disposed
                .isMustBeDisposed(true)
                // then priceInCents and quality
                // do not matter anymore :)
                .forecastedPriceInCents(0)
                .forecastedQuality(0);
        final Duration expMinusTs = Duration.between(
                timestamp,
                getBasicProductInformation().getExpirationTimestamp()
        );
        if (expMinusTs.isZero() || expMinusTs.isNegative()) {
            return b.comment("the product has expired").build();
        }
        if (expMinusTs.compareTo(FIFTY_DAYS) < 0) {
            return b.comment(
                    "expiration date must be at least in %s days from the given date".formatted(FIFTY_DAYS.toDays())
            ).build();
        }
        if (expMinusTs.compareTo(HUNDRED_DAYS) > 0) {
            return b.comment(
                    "expiration date must be at most in %s days from the given date".formatted(HUNDRED_DAYS.toDays())
            ).build();
        }
        ForecastUtil.checkForecastIsInFuture(getPlacementTimestamp(), timestamp);
        final Duration tsMinusPlacementTs = Duration.between(getPlacementTimestamp(), timestamp);
        final int quality = (int) Math.max(getBasicProductInformation().getStartQuality() - tsMinusPlacementTs.toDays(), 0);
        if (quality < MIN_QUALITY) {
            return b.forecastedQuality(quality)
                    .comment("quality is less than minimal quality (%s)".formatted(MIN_QUALITY))
                    .build();
        }
        final int priceInCents = PriceCalculationUtil.getGeneralDailyPriceInCents(
                getBasicProductInformation().getBasePriceInCents(),
                quality
        );
        return b.isMustBeDisposed(false)
                .forecastedPriceInCents(priceInCents)
                .forecastedQuality(quality)
                .comment("OK")
                .build();
    }
}

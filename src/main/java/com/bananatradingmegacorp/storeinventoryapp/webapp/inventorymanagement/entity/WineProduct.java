package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import java.time.Duration;
import java.time.Instant;

public class WineProduct extends AbstractProduct {
    public static final String NAME = "wine";

    public WineProduct(
            final BasicProductInformationModel basicProductInformation,
            final Instant placementTimestamp) {
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
        if (getBasicProductInformation().getStartQuality() < 0) {
            return b.comment("wine must have non-negative quality").build();
        }
        final Duration tsMinusExp = Duration.between(
                getBasicProductInformation().getExpirationTimestamp(),
                timestamp
        );
        final int quality;
        if (tsMinusExp.isNegative()) {
            quality = getBasicProductInformation().getStartQuality();
        } else {
            quality = (int) Math.min(
                    getBasicProductInformation().getStartQuality() + (tsMinusExp.toDays() / 10),
                    50
            );
        }
        return b.isMustBeDisposed(false)
                // wine's price does not change
                .forecastedPriceInCents(getBasicProductInformation().getBasePriceInCents())
                // however, quality may still rise :)
                .forecastedQuality(quality)
                .comment("OK")
                .build();
    }
}

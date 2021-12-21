package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;

@lombok.Data
@lombok.Builder
@Jacksonized
public class ForecastedProductInformationModel {
    @NonNull
    @PositiveOrZero
    private final Integer id;

    @NonNull
    @NotBlank
    private final String name;

    @NonNull
    @NotBlank
    private final String description;

    @NonNull
    private final Instant expirationTimestamp;

    @NonNull
    @PositiveOrZero
    private final Integer forecastedPriceInCents;

    @NonNull
    @PositiveOrZero
    private final Integer forecastedQuality;

    @NonNull
    private final Boolean isMustBeDisposed;

    @NonNull
    private final String comment;

    public static ForecastedProductInformationModel.ForecastedProductInformationModelBuilder buildFrom(
            final BasicProductInformationModel basicProductInformationModel
    ) {
        return ForecastedProductInformationModel.builder()
                .id(basicProductInformationModel.getId())
                .name(basicProductInformationModel.getName())
                .description(basicProductInformationModel.getDescription())
                .expirationTimestamp(basicProductInformationModel.getExpirationTimestamp())
                .comment("");
    }
}

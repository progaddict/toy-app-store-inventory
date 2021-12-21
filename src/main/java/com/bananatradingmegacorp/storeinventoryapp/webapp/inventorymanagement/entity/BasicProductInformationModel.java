package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;

@lombok.Data
@lombok.Builder
@Jacksonized
public class BasicProductInformationModel {
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
    private final Integer basePriceInCents;

    @NonNull
    @PositiveOrZero
    private final Integer startQuality;
}

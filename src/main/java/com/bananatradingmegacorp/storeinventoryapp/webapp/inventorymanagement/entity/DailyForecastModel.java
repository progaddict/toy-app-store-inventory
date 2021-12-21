package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

@lombok.Data
@lombok.Builder
@Jacksonized
public class DailyForecastModel {
    private final Instant timestamp;
    private final List<ForecastedProductInformationModel> forecasts;
}

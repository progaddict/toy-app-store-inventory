package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WineProductTest {
    @ParameterizedTest
    @MethodSource("dataForGetForecastedProductInformationModel")
    void getForecastedProductInformationModel(
            // GIVEN
            final BasicProductInformationModel bpi,
            final Instant placementTimestamp,
            final Instant timestamp,
            final ForecastedProductInformationModel expectedForecast
    ) {
        final WineProduct sut = new WineProduct(bpi, placementTimestamp);
        // WHEN
        final ForecastedProductInformationModel forecast = sut.getForecastedProductInformationModel(timestamp);
        // THEN
        assertEquals(expectedForecast, forecast);
    }

    static Stream<Arguments> dataForGetForecastedProductInformationModel() {
        // case 01: wine with negative quality must be disposed
        final BasicProductInformationModel bpi01 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-31T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(-10)
                .build();
        final Instant pTs01 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts01 = Instant.parse("2021-12-05T10:00:00.000Z");
        final ForecastedProductInformationModel ef01 = ForecastedProductInformationModel.buildFrom(bpi01)
                .isMustBeDisposed(true)
                .comment("wine must have non-negative quality")
                .forecastedPriceInCents(0)
                .forecastedQuality(0)
                .build();
        final Arguments case01 = Arguments.of(bpi01, pTs01, ts01, ef01);

        // case 02: zero quality is however OK
        final BasicProductInformationModel bpi02 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-31T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(0)
                .build();
        final Instant pTs02 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts02 = Instant.parse("2021-12-05T10:00:00.000Z");
        final ForecastedProductInformationModel ef02 = ForecastedProductInformationModel.buildFrom(bpi02)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(10_00)
                .forecastedQuality(0)
                .build();
        final Arguments case02 = Arguments.of(bpi02, pTs02, ts02, ef02);

        // case 03: quality before expiration stays the same
        final BasicProductInformationModel bpi03 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-31T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(30)
                .build();
        final Instant pTs03 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts03 = Instant.parse("2021-12-30T10:00:00.000Z");
        final ForecastedProductInformationModel ef03 = ForecastedProductInformationModel.buildFrom(bpi03)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(10_00)
                .forecastedQuality(30)
                .build();
        final Arguments case03 = Arguments.of(bpi03, pTs03, ts03, ef03);

        // case 04: quality after expiration increases
        final BasicProductInformationModel bpi04 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-31T10:00:00.000Z"))
                .basePriceInCents(15_00)
                .startQuality(30)
                .build();
        final Instant pTs04 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts04 = Instant.parse("2022-01-30T10:00:00.000Z");
        final ForecastedProductInformationModel ef04 = ForecastedProductInformationModel.buildFrom(bpi04)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(15_00)
                .forecastedQuality(33)
                .build();
        final Arguments case04 = Arguments.of(bpi04, pTs04, ts04, ef04);

        // case 05: quality should max out
        final BasicProductInformationModel bpi05 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-31T10:00:00.000Z"))
                .basePriceInCents(15_00)
                .startQuality(30)
                .build();
        final Instant pTs05 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts05 = Instant.parse("3000-01-01T10:00:00.000Z");
        final ForecastedProductInformationModel ef05 = ForecastedProductInformationModel.buildFrom(bpi05)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(15_00)
                .forecastedQuality(50)
                .build();
        final Arguments case05 = Arguments.of(bpi05, pTs05, ts05, ef05);

        return Stream.of(
                case01,
                case02,
                case03,
                case04,
                case05
        );
    }

    static BasicProductInformationModel.BasicProductInformationModelBuilder bpimBuilder() {
        return BasicProductInformationModel.builder()
                .id(0)
                .name(WineProduct.NAME)
                .description("test wine bottle");
    }
}

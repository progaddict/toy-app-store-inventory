package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheeseProductTest {
    @ParameterizedTest
    @MethodSource("dataForGetForecastedProductInformationModel")
    void getForecastedProductInformationModel(
            // GIVEN
            final BasicProductInformationModel bpi,
            final Instant placementTimestamp,
            final Instant timestamp,
            final ForecastedProductInformationModel expectedForecast
    ) {
        final CheeseProduct sut = new CheeseProduct(bpi, placementTimestamp);
        // WHEN
        final ForecastedProductInformationModel forecast = sut.getForecastedProductInformationModel(timestamp);
        // THEN
        // this works because @lombok.Data generates equals method :)
        assertEquals(expectedForecast, forecast);
    }

    @Test
    void forecastMustLieInFuture() {
        // GIVEN
        final BasicProductInformationModel bpi = bpimBuilder()
                .expirationTimestamp(Instant.parse("2022-02-10T10:00:00.000Z"))
                .basePriceInCents(15_00)
                .startQuality(100)
                .build();
        final Instant pTs = Instant.parse("2021-12-10T10:00:00.000Z");
        final Instant ts = Instant.parse("2021-12-09T10:00:00.000Z");
        final CheeseProduct sut = new CheeseProduct(bpi, pTs);
        // WHEN
        final RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> sut.getForecastedProductInformationModel(ts)
        );
        // THEN
        assertEquals("forecast timestamp (2021-12-09T10:00:00Z) is behind the placement timestamp (2021-12-10T10:00:00Z)", error.getMessage());
    }

    static Stream<Arguments> dataForGetForecastedProductInformationModel() {
        // case 01: product has already expired, even before placement
        final BasicProductInformationModel bpi01 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-21T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(100)
                .build();
        final Instant pTs01 = Instant.parse("2021-12-22T10:00:00.000Z");
        final Instant ts01 = Instant.parse("2021-12-23T10:00:00.000Z");
        final ForecastedProductInformationModel ef01 = ForecastedProductInformationModel.buildFrom(bpi01)
                .isMustBeDisposed(true)
                .comment("the product has expired")
                .forecastedPriceInCents(0)
                .forecastedQuality(0)
                .build();
        final Arguments case01 = Arguments.of(bpi01, pTs01, ts01, ef01);

        // case 02: product expires in less than 50 days
        final BasicProductInformationModel bpi02 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2021-12-30T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(100)
                .build();
        final Instant pTs02 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts02 = Instant.parse("2021-12-21T10:00:00.000Z");
        final ForecastedProductInformationModel ef02 = ForecastedProductInformationModel.buildFrom(bpi02)
                .isMustBeDisposed(true)
                .comment("expiration date must be at least in 50 days from the given date")
                .forecastedPriceInCents(0)
                .forecastedQuality(0)
                .build();
        final Arguments case02 = Arguments.of(bpi02, pTs02, ts02, ef02);

        // case 03: product expires in more than 100 days
        final BasicProductInformationModel bpi03 = bpimBuilder()
                .expirationTimestamp(Instant.parse("3021-12-30T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(100)
                .build();
        final Instant pTs03 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts03 = Instant.parse("2021-12-21T10:00:00.000Z");
        final ForecastedProductInformationModel ef03 = ForecastedProductInformationModel.buildFrom(bpi03)
                .isMustBeDisposed(true)
                .comment("expiration date must be at most in 100 days from the given date")
                .forecastedPriceInCents(0)
                .forecastedQuality(0)
                .build();
        final Arguments case03 = Arguments.of(bpi03, pTs03, ts03, ef03);

        // case 04: quality degrades over time and product expires because of that
        final BasicProductInformationModel bpi04 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2022-02-15T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(42)
                .build();
        final Instant pTs04 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts04 = Instant.parse("2021-12-14T10:00:00.000Z");
        final ForecastedProductInformationModel ef04 = ForecastedProductInformationModel.buildFrom(bpi04)
                .isMustBeDisposed(true)
                .comment("quality is less than minimal quality (30)")
                .forecastedPriceInCents(0)
                .forecastedQuality(29)
                .build();
        final Arguments case04 = Arguments.of(bpi04, pTs04, ts04, ef04);

        // case 05: quality degrades over time and influences price calculation
        final BasicProductInformationModel bpi05 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2022-02-15T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(99)
                .build();
        final Instant pTs05 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts05 = Instant.parse("2021-12-14T10:00:00.000Z");
        final ForecastedProductInformationModel ef05 = ForecastedProductInformationModel.buildFrom(bpi05)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(18_60)
                .forecastedQuality(86)
                .build();
        final Arguments case05 = Arguments.of(bpi05, pTs05, ts05, ef05);

        // case 06: quality degrades over time and influences price calculation
        final BasicProductInformationModel bpi06 = bpimBuilder()
                .expirationTimestamp(Instant.parse("2022-03-15T10:00:00.000Z"))
                .basePriceInCents(10_00)
                .startQuality(99)
                .build();
        final Instant pTs06 = Instant.parse("2021-12-01T10:00:00.000Z");
        final Instant ts06 = Instant.parse("2021-12-30T10:00:00.000Z");
        final ForecastedProductInformationModel ef06 = ForecastedProductInformationModel.buildFrom(bpi06)
                .isMustBeDisposed(false)
                .comment("OK")
                .forecastedPriceInCents(17_00)
                .forecastedQuality(70)
                .build();
        final Arguments case06 = Arguments.of(bpi06, pTs06, ts06, ef06);

        return Stream.of(
                case01,
                case02,
                case03,
                case04,
                case05,
                case06
        );
    }

    private static BasicProductInformationModel.BasicProductInformationModelBuilder bpimBuilder() {
        return BasicProductInformationModel.builder()
                .id(0)
                .name(CheeseProduct.NAME)
                .description("test cheese sample");
    }
}

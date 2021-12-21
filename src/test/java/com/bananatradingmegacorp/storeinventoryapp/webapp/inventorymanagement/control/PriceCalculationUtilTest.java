package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculationUtilTest {
    @ParameterizedTest
    @MethodSource("dataForGetGeneralDailyPriceInCents")
    void getGeneralDailyPriceInCents(
            // GIVEN
            final int basePriceInCents,
            final int quality,
            final int expectedPriceInCents
    ) {
        // WHEN
        final int priceInCents = PriceCalculationUtil.getGeneralDailyPriceInCents(
                basePriceInCents,
                quality
        );
        // THEN
        assertEquals(expectedPriceInCents, priceInCents);
    }

    static Stream<Arguments> dataForGetGeneralDailyPriceInCents() {
        return Stream.of(
                Arguments.of(100, 100, 1100),
                Arguments.of(0, 0, 0),
                Arguments.of(2500, 0, 2500),
                Arguments.of(3500, 20, 3700)
        );
    }
}
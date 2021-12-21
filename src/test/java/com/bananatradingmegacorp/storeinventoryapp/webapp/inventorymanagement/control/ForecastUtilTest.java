package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForecastUtilTest {
    @Test
    void sameTimestampIsOk() {
        ForecastUtil.checkForecastIsInFuture(
                Instant.parse("2021-12-10T10:00:00.000Z"),
                Instant.parse("2021-12-10T10:00:00.000Z")
        );
        // no exception should be thrown
    }

    @Test
    void futureForecastIsOk() {
        ForecastUtil.checkForecastIsInFuture(
                Instant.parse("2020-01-09T10:00:00.000Z"),
                Instant.parse("2021-12-10T10:00:00.000Z")
        );
        // no exception should be thrown
    }

    @Test
    void forecastInPastShouldThrow() {
        final RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> ForecastUtil.checkForecastIsInFuture(
                        Instant.parse("2021-12-01T10:00:00.000Z"),
                        Instant.parse("2021-01-01T10:00:00.000Z")
                )
        );
        assertEquals("forecast timestamp (2021-01-01T10:00:00Z) is behind the placement timestamp (2021-12-01T10:00:00Z)", error.getMessage());
    }
}
package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ForecastedProductInformationModel;

import java.time.Instant;

public interface Product {
    BasicProductInformationModel getBasicProductInformation();

    /**
     * When product has been placed on the shelf.
     */
    Instant getPlacementTimestamp();

    ForecastedProductInformationModel getForecastedProductInformationModel(final Instant timestamp);
}

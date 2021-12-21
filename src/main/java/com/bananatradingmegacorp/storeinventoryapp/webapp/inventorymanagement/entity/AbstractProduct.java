package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control.Product;

import java.time.Instant;
import java.util.Objects;

public abstract class AbstractProduct implements Product {
    private final BasicProductInformationModel basicProductInformation;
    private final Instant placementTimestamp;

    protected AbstractProduct(
            final BasicProductInformationModel basicProductInformation,
            final Instant placementTimestamp
    ) {
        this.basicProductInformation = Objects.requireNonNull(basicProductInformation);
        this.placementTimestamp = Objects.requireNonNull(placementTimestamp);
    }

    @Override
    public BasicProductInformationModel getBasicProductInformation() {
        return basicProductInformation;
    }

    @Override
    public Instant getPlacementTimestamp() {
        return placementTimestamp;
    }
}

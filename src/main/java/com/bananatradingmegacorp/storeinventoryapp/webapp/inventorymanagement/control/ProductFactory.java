package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;

import java.time.Instant;

public interface ProductFactory {
    Product getProduct(
            final BasicProductInformationModel basicProductInformationModel,
            final Instant placementTimestamp
    );
}

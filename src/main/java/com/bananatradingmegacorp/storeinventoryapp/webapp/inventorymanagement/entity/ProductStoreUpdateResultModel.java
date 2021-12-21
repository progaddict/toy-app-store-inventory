package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity;

import lombok.extern.jackson.Jacksonized;

import java.util.List;

@lombok.Data
@lombok.Builder
@Jacksonized
public class ProductStoreUpdateResultModel {
    private final List<BasicProductInformationModel> addedProducts;
    private final List<BasicProductInformationModel> immediatelyDisposedProducts;
    private final List<BasicProductInformationModel> disposedOldProducts;
}

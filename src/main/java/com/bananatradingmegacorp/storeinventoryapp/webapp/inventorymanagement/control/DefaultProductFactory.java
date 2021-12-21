package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.CheeseProduct;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.WineProduct;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DefaultProductFactory implements ProductFactory {
    @Override
    public Product getProduct(
            final BasicProductInformationModel basicProductInformation,
            final Instant placementTimestamp
    ) {
        switch (basicProductInformation.getName()) {
            case CheeseProduct.NAME:
                return new CheeseProduct(basicProductInformation, placementTimestamp);
            case WineProduct.NAME:
                return new WineProduct(basicProductInformation, placementTimestamp);
        }
        throw new RuntimeException("could not produce product for " + basicProductInformation);
    }
}

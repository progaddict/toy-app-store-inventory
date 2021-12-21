package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.DailyForecastModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ForecastedProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ProductStoreUpdateResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@lombok.RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoreProductsManager {
    private final Clock clock;
    private final ProductFactory productFactory;
    private final ProductInMemoryStorage productInMemoryStorage;

    // If we would have a DB then:
    //@Transactional(readOnly = true)
    public List<BasicProductInformationModel> getAllProducts() {
        return new ArrayList<>(
                productInMemoryStorage.get().stream()
                        .map(Product::getBasicProductInformation)
                        .toList()
        );
    }

    //@Transactional
    public void deleteAllProducts() {
        productInMemoryStorage.deleteAll();
    }

    //@Transactional
    public ProductStoreUpdateResultModel addNewProducts(
            final Collection<BasicProductInformationModel> allNewProducts
    ) {
        final Instant now = clock.instant();
        // add new
        productInMemoryStorage.add(
                allNewProducts.stream()
                        .map(p -> productFactory.getProduct(p, now))
                        .toList()
        );
        // dispose new and old
        final List<BasicProductInformationModel> disposedProducts = disposeProducts(now)
                .stream()
                .map(Product::getBasicProductInformation)
                .toList();
        // calculate newly added, immediately disposed and old disposed
        final Set<Integer> disposedProductsIds = disposedProducts.stream()
                .map(BasicProductInformationModel::getId)
                .collect(Collectors.toSet());
        List<BasicProductInformationModel> addedProducts = allNewProducts.stream()
                .filter(p -> !disposedProductsIds.contains(p.getId()))
                .toList();
        List<BasicProductInformationModel> immediatelyDisposedProducts = allNewProducts.stream()
                .filter(p -> disposedProductsIds.contains(p.getId()))
                .toList();
        final Set<Integer> allNewProductsIds = allNewProducts.stream()
                .map(BasicProductInformationModel::getId)
                .collect(Collectors.toSet());
        final List<BasicProductInformationModel> disposedOldProducts = disposedProducts.stream()
                .filter(p -> !allNewProductsIds.contains(p.getId()))
                .toList();
        return ProductStoreUpdateResultModel.builder()
                .addedProducts(addedProducts)
                .immediatelyDisposedProducts(immediatelyDisposedProducts)
                .disposedOldProducts(disposedOldProducts)
                .build();
    }

    //@Transactional(readOnly = true)
    public List<DailyForecastModel> getForecast(final int daysToForecast) {
        final Instant now = clock.instant();
        final List<DailyForecastModel> result = new ArrayList<>();
        final List<Product> allProducts = productInMemoryStorage.get();
        final Set<Integer> notYetPlacedProductsIds = allProducts.stream()
                .filter(p -> now.compareTo(p.getPlacementTimestamp()) < 0)
                .map(Product::getBasicProductInformation)
                .map(BasicProductInformationModel::getId)
                .collect(Collectors.toSet());
        if (!notYetPlacedProductsIds.isEmpty()) {
            LOG.warn(
                    "as of now ({})"
                            + " the products with the following IDs are not yet on the shelf"
                            + " and they are going to be excluded from calculations: {}",
                    now,
                    notYetPlacedProductsIds.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining(","))
            );
        }
        List<Product> products = allProducts.stream()
                .filter(p -> !notYetPlacedProductsIds.contains(p.getBasicProductInformation().getId()))
                .toList();
        int day = 0;
        while (day <= daysToForecast && !products.isEmpty()) {
            final Instant timestamp = now.plus(day, ChronoUnit.DAYS);
            final List<ForecastedProductInformationModel> forecasts = products.stream()
                    .map(p -> p.getForecastedProductInformationModel(timestamp))
                    .collect(Collectors.toList());
            result.add(
                    DailyForecastModel.builder()
                            .timestamp(timestamp)
                            .forecasts(forecasts)
                            .build()
            );
            final Set<Integer> disposedIds = forecasts.stream()
                    .filter(ForecastedProductInformationModel::getIsMustBeDisposed)
                    .map(ForecastedProductInformationModel::getId)
                    .collect(Collectors.toSet());
            products = products.stream()
                    .filter(p -> !disposedIds.contains(p.getBasicProductInformation().getId()))
                    .collect(Collectors.toList());
            day++;
        }
        return result;
    }

    private List<Product> disposeProducts(final Instant timestamp) {
        final List<Product> allProducts = productInMemoryStorage.get();
        final List<Product> mustBeDisposed = allProducts.stream()
                .filter(p -> p.getForecastedProductInformationModel(timestamp).getIsMustBeDisposed())
                .collect(Collectors.toList());
        productInMemoryStorage.delete(mustBeDisposed);
        return mustBeDisposed;
    }
}

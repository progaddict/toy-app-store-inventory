package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Poor man's DB :)
 */
@Service
public class ProductInMemoryStorage {
    private final ConcurrentHashMap<Integer, Product> currentlyStoredProducts = new ConcurrentHashMap<>();

    public List<Product> get() {
        synchronized (currentlyStoredProducts) {
            return new ArrayList<>(currentlyStoredProducts.values());
        }
    }

    public void add(final Collection<Product> products) {
        if (Objects.isNull(products) || products.isEmpty()) {
            return;
        }
        synchronized (currentlyStoredProducts) {
            for (final Product p : products) {
                final int id = p.getBasicProductInformation().getId();
                currentlyStoredProducts.put(id, p);
            }
        }
    }

    public void deleteAll() {
        synchronized (currentlyStoredProducts) {
            currentlyStoredProducts.clear();
        }
    }

    public void delete(final Collection<Product> products) {
        if (Objects.isNull(products) || products.isEmpty()) {
            return;
        }
        final Set<Integer> ids = products.stream()
                .map(Product::getBasicProductInformation)
                .map(BasicProductInformationModel::getId)
                .collect(Collectors.toSet());
        synchronized (currentlyStoredProducts) {
            ids.forEach(currentlyStoredProducts::remove);
        }
    }
}

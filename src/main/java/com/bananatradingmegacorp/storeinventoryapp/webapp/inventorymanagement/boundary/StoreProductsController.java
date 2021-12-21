package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.boundary;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control.ProductParser;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control.StoreProductsManager;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.DailyForecastModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ProductStoreUpdateResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(StoreProductsController.URL_PREFIX)
public class StoreProductsController {
    public static final String URL_PREFIX = "/api/v1/product";

    private final StoreProductsManager storeProductsManager;
    private final ProductParser<InputStream> csvProductParser;

    @Autowired
    public StoreProductsController(
            final StoreProductsManager storeProductsManager,
            @Qualifier("csvProductParser") final ProductParser<InputStream> csvProductParser
    ) {
        this.storeProductsManager = storeProductsManager;
        this.csvProductParser = csvProductParser;
    }

    @GetMapping
    public List<BasicProductInformationModel> getAllProducts() {
        return storeProductsManager.getAllProducts();
    }

    @PostMapping
    public ProductStoreUpdateResultModel addProducts(
            @NonNull @Valid @RequestBody final List<BasicProductInformationModel> newProducts
    ) {
        return storeProductsManager.addNewProducts(newProducts);
    }

    @DeleteMapping
    public List<BasicProductInformationModel> clearShelf() {
        final List<BasicProductInformationModel> products = storeProductsManager.getAllProducts();
        storeProductsManager.deleteAllProducts();
        return products;
    }

    @GetMapping("/forecast")
    public List<DailyForecastModel> getForecast(
            @NonNull @Min(1) @Max(100)
            @RequestParam(name = "days", defaultValue = "3") final Integer daysToForecast
    ) {
        return storeProductsManager.getForecast(daysToForecast);
    }

    @PostMapping("/csv")
    public HttpEntity<ProductStoreUpdateResultModel> uploadCsv(
            final @NotNull @RequestParam("file") MultipartFile file
    ) {
        LOG.debug("received CSV");
        try {
            final List<BasicProductInformationModel> newProducts = csvProductParser.apply(file.getInputStream());
            LOG.debug("parsed CSV, saving products: {}", newProducts);
            return ResponseEntity.ok(storeProductsManager.addNewProducts(newProducts));
        } catch (final IOException error) {
            LOG.error("failed to read CSV file", error);
            return ResponseEntity.internalServerError().build();
        }
    }
}

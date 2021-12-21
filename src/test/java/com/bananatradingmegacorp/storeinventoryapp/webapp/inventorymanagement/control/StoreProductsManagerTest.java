package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ProductStoreUpdateResultModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * Example of a unit test with mocks.
 */
@ExtendWith(MockitoExtension.class)
class StoreProductsManagerTest {
    @Mock
    private Clock clock;
    @Mock
    private ProductFactory productFactory;
    @Mock
    private ProductInMemoryStorage productInMemoryStorage;
    @InjectMocks
    private StoreProductsManager sut;

    @Test
    void getAllProducts() {
        // GIVEN
        final Product product = Mockito.mock(Product.class);
        final BasicProductInformationModel bpim = BasicProductInformationModel.builder()
                .id(1)
                .name("cheese")
                .description("test cheese 1")
                .expirationTimestamp(Instant.now())
                .basePriceInCents(100)
                .startQuality(100)
                .build();
        Mockito.when(product.getBasicProductInformation()).thenReturn(bpim);
        Mockito.when(productInMemoryStorage.get()).thenReturn(List.of(product));
        // WHEN
        final List<BasicProductInformationModel> result = sut.getAllProducts();
        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(bpim);
    }

    @Test
    void addNewProducts() {
        // GIVEN
        // via Clock one can mock time and pretend it happened yesterday :)
        final Instant yesterday = Instant.now().minus(Duration.ofDays(1));
        Mockito.when(clock.instant()).thenReturn(yesterday);

        final Product product = Mockito.mock(Product.class);
        Mockito.when(productFactory.getProduct(any(), any())).thenReturn(product);

        // suppose nothing is stored
        Mockito.when(productInMemoryStorage.get()).thenReturn(List.of());

        final BasicProductInformationModel bpim = BasicProductInformationModel.builder()
                .id(2)
                .name("cheese")
                .description("test cheese 2")
                .expirationTimestamp(Instant.now())
                .basePriceInCents(200)
                .startQuality(200)
                .build();
        final List<BasicProductInformationModel> newProducts = List.of(bpim);

        // WHEN
        final ProductStoreUpdateResultModel result = sut.addNewProducts(newProducts);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getAddedProducts()).hasSize(1);
        assertThat(result.getAddedProducts().get(0)).isEqualTo(bpim);
        assertThat(result.getImmediatelyDisposedProducts()).isEmpty();
        assertThat(result.getDisposedOldProducts()).isEmpty();

        Mockito.verify(clock).instant();
        Mockito.verify(productFactory).getProduct(bpim, yesterday);
        Mockito.verify(productInMemoryStorage).add(argThat(products -> {
            assertThat(products).isNotNull();
            assertThat(products).hasSize(1);
            assertThat(products.iterator().next()).isEqualTo(product);
            return true;
        }));
        Mockito.verify(productInMemoryStorage).get();
        Mockito.verify(productInMemoryStorage).delete(argThat(products -> {
            assertThat(products).isNotNull();
            assertThat(products).isEmpty();
            return true;
        }));
    }
}
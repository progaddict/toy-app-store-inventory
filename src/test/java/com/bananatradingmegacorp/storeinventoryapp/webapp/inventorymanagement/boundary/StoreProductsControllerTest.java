package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.boundary;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.DailyForecastModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ForecastedProductInformationModel;
import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.ProductStoreUpdateResultModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StoreProductsControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://127.0.0.1:" + port;
    }

    /**
     * Example of a simple integration test.
     * For more complex cases I'd use
     * <a href="https://citrusframework.org/">Citrus Framework</a>.
     */
    @Test
    void simpleIntegrationTest() {
        // at the beginning there's nothing in there
        assertThat(getProducts()).isEmpty();

        // add some products
        final List<BasicProductInformationModel> request01 = List.of(
                BasicProductInformationModel.builder()
                        .id(10)
                        .name("cheese")
                        .description("test cheese 1")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(70)))
                        .basePriceInCents(700)
                        .startQuality(40)
                        .build(),
                BasicProductInformationModel.builder()
                        .id(20)
                        .name("cheese")
                        .description("test cheese 2")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(75)))
                        .basePriceInCents(800)
                        .startQuality(50)
                        .build(),
                BasicProductInformationModel.builder()
                        .id(30)
                        .name("cheese")
                        .description("test cheese 3")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(20)))
                        .basePriceInCents(300)
                        .startQuality(60)
                        .build(),
                BasicProductInformationModel.builder()
                        .id(40)
                        .name("wine")
                        .description("test wine 1")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(5)))
                        .basePriceInCents(1000)
                        .startQuality(10)
                        .build(),
                BasicProductInformationModel.builder()
                        .id(50)
                        .name("wine")
                        .description("test wine 2")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(15)))
                        .basePriceInCents(1500)
                        .startQuality(15)
                        .build()
        );
        final ProductStoreUpdateResultModel response01 = restTemplate.postForObject(
                baseUrl + StoreProductsController.URL_PREFIX,
                request01,
                ProductStoreUpdateResultModel.class
        );
        assertThat(response01).isNotNull();
        // 2 cheese products and 2 wines have been added
        assertThat(
                response01.getAddedProducts()
                        .stream()
                        .map(BasicProductInformationModel::getId)
                        .collect(Collectors.toSet())
        ).isEqualTo(Set.of(10, 20, 40, 50));
        // cheese with ID 30 has been immediately disposed
        // because its' expiration date is too small
        assertThat(
                response01.getImmediatelyDisposedProducts()
                        .stream()
                        .map(BasicProductInformationModel::getId)
                        .collect(Collectors.toSet())
        ).isEqualTo(Set.of(30));
        // there are no old products
        assertThat(response01.getDisposedOldProducts()).isEmpty();

        // reread products and check they have been saved
        assertThat(
                getProducts().stream()
                        .map(BasicProductInformationModel::getId)
                        .collect(Collectors.toSet())
        ).isEqualTo(Set.of(10, 20, 40, 50));

        /* !!! ACHTUNG !!! A DISCLAIMER AHEAD :D

        YES, I KNOW THAT THIS BEHAVIOR IS NOT REALLY IDEAL
        WHEN A SYSTEM REPORTS THAT SOME OF THE PRODUCTS
        HAS BEEN IMMEDIATELY DISPOSED (THE EXAMPLE ABOVE)
        AND AT THE SAME TIME RETURNS 400 BAD REQUEST
        FOR SOME OF THE OTHER SIMILAR CASES.

        HOWEVER, HERE WE HAVE A SOMEWHAT BLURRED LINE
        BETWEEN BUSINESS LOGIC VALIDATION AND TECHNICAL VALIDATION.
        EXAMPLE OF A BUSINESS LOGIC VALIDATION WOULD BE THE RULE
        WHICH FORBIDS CHEESES WHICH EXPIRE IN E.G. 120 DAYS.
        SUCH CASES ARE USUALLY AUDITED I.E. THEY ARE REPORTED
        TO SOME EXTERNAL SYSTEM THAT THERE'S BEEN A VIOLATION OF
        BUSINESS LOGIC.

        TECHNICAL VALIDATION IS E.G. NOT-NULL-VALIDATION
        OR FORBIDDING OF NEGATIVE VALUES FOR PRICES (COMMON SENSE).

        I'VE ASSUMED THAT IF TECHNICAL VALIDATION FAILS
        THEN PROCESSING SHOULD BE STOPPED AND CLIENT
        SHOULD RECEIVE AN ERROR (I.E. 400 BAD REQUEST)
        AND IF BUSINESS VALIDATION FAILS THEN PROCESSING
        SHOULD GO ON FOR OTHER POSSIBLY VALID ITEMS.
        */
        // negative price is invalid
        final List<BasicProductInformationModel> request02 = List.of(
                BasicProductInformationModel.builder()
                        .id(1000)
                        .name("cheese")
                        .description("invalid cheese having negative price")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(70)))
                        .basePriceInCents(-100)
                        .startQuality(45)
                        .build()
        );
        final ResponseEntity<String> response02 = restTemplate.postForEntity(
                baseUrl + StoreProductsController.URL_PREFIX,
                request02,
                String.class
        );
        assertThat(response02.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // negative quality is also invalid
        final List<BasicProductInformationModel> request03 = List.of(
                BasicProductInformationModel.builder()
                        .id(1000)
                        .name("wine")
                        .description("invalid wine with negative quality")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(70)))
                        .basePriceInCents(1000)
                        .startQuality(-45)
                        .build()
        );
        final ResponseEntity<String> response03 = restTemplate.postForEntity(
                baseUrl + StoreProductsController.URL_PREFIX,
                request03,
                String.class
        );
        assertThat(response03.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // name must not be blank
        final List<BasicProductInformationModel> request04 = List.of(
                BasicProductInformationModel.builder()
                        .id(1000)
                        .name("")
                        .description("wine would be OK but there is no product name :)")
                        .expirationTimestamp(Instant.now().plus(Duration.ofDays(1000)))
                        .basePriceInCents(1500)
                        .startQuality(35)
                        .build()
        );
        final ResponseEntity<String> response04 = restTemplate.postForEntity(
                baseUrl + StoreProductsController.URL_PREFIX,
                request04,
                String.class
        );
        assertThat(response04.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // make a forecast for the next 100 days
        final List<DailyForecastModel> response05 = Arrays.stream(
                restTemplate.getForObject(
                        baseUrl + StoreProductsController.URL_PREFIX + "/forecast?days=100",
                        DailyForecastModel[].class
                )
        ).toList();
        assertThat(response05).isNotNull();
        // today + 100 next days = 101 days in total
        assertThat(response05).hasSize(101);
        // yes, indeed, 100 days in duration
        assertThat(
                Duration.between(response05.get(0).getTimestamp(), response05.get(100).getTimestamp())
                        .toDays()
        ).isEqualTo(100);
        // in the beginning there are wine and cheese
        assertThat(
                response05.get(0).getForecasts()
                        .stream()
                        .map(ForecastedProductInformationModel::getId)
                        .collect(Collectors.toSet())
        ).isEqualTo(Set.of(10, 20, 40, 50));
        // but in the end only wine "survives" :)
        assertThat(
                response05.get(100).getForecasts()
                        .stream()
                        .map(ForecastedProductInformationModel::getId)
                        .collect(Collectors.toSet())
        ).isEqualTo(Set.of(40, 50));

        // delete all products
        restTemplate.delete(baseUrl + StoreProductsController.URL_PREFIX);
        assertThat(getProducts()).isEmpty();
    }

    private List<BasicProductInformationModel> getProducts() {
        final BasicProductInformationModel[] response = restTemplate.getForObject(
                baseUrl + StoreProductsController.URL_PREFIX,
                BasicProductInformationModel[].class
        );
        return Arrays.stream(response).toList();
    }
}

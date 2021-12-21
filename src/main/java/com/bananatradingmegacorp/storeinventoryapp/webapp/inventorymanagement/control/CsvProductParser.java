package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Qualifier("csvProductParser")
@Slf4j
public class CsvProductParser implements ProductParser<InputStream> {
    private static final int COL_COUNT = 6;
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_EXPIRATION_TIMESTAMP = "expirationTimestamp";
    private static final String COL_BASE_PRICE_IN_CENTS = "basePriceInCents";
    private static final String COL_START_QUALITY = "startQuality";
    private static final Set<String> COLS = Set.of(
            COL_ID,
            COL_NAME,
            COL_DESCRIPTION,
            COL_EXPIRATION_TIMESTAMP,
            COL_BASE_PRICE_IN_CENTS,
            COL_START_QUALITY
    );

    @Override
    public List<BasicProductInformationModel> apply(final InputStream inputStream) {
        try (
                final InputStreamReader isr = new InputStreamReader(inputStream);
                final BufferedReader br = new BufferedReader(isr);
                final CSVReader csvReader = new CSVReader(br)
        ) {
            final String[] header = csvReader.readNext();
            if (Objects.isNull(header)) {
                throw new RuntimeException("no header");
            }
            if (header.length != COL_COUNT) {
                throw new RuntimeException("wrong header: " + String.join(",", header));
            }
            final Set<String> missingCols = new HashSet<>(COLS);
            missingCols.removeAll(Set.of(header));
            if (!missingCols.isEmpty()) {
                throw new RuntimeException("missing columns: " + String.join(",", missingCols));
            }
            final List<BasicProductInformationModel> result = new ArrayList<>();
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                final BasicProductInformationModel.BasicProductInformationModelBuilder b = BasicProductInformationModel.builder();
                for (int i = 0; i < row.length; i++) {
                    final String colValue = row[i];
                    final String colName = header[i];
                    switch (colName) {
                        case COL_ID -> b.id(Integer.parseInt(colValue));
                        case COL_NAME -> b.name(colValue);
                        case COL_DESCRIPTION -> b.description(colValue);
                        case COL_EXPIRATION_TIMESTAMP -> b.expirationTimestamp(Instant.parse(colValue));
                        case COL_BASE_PRICE_IN_CENTS -> b.basePriceInCents(Integer.parseInt(colValue));
                        case COL_START_QUALITY -> b.startQuality(Integer.parseInt(colValue));
                    }
                }
                result.add(b.build());
            }
            return result;
        } catch (final IOException | CsvValidationException error) {
            LOG.error("failed to read CSV", error);
            throw new RuntimeException(error);
        }
    }
}

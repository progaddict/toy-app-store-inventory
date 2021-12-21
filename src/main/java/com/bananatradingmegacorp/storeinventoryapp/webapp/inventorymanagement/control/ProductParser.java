package com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.control;

import com.bananatradingmegacorp.storeinventoryapp.webapp.inventorymanagement.entity.BasicProductInformationModel;

import java.util.List;
import java.util.function.Function;

public interface ProductParser<T> extends Function<T, List<BasicProductInformationModel>> {
}

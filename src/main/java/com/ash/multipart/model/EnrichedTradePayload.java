package com.ash.multipart.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Hold the enriched trade data after looking up the product id in the product map
 */
public class EnrichedTradePayload {

    private String parsedDate;
    private String productName;
    private String currency;
    private BigDecimal price;

    // holds the id to product mapping
    private Map<Long, Product> productMap = Collections.emptyMap();

    public EnrichedTradePayload() {
    }

    public EnrichedTradePayload(String parsedDate, String productName, String currency, BigDecimal price) {
        this.parsedDate = parsedDate;
        this.productName = productName;
        this.currency = currency;
        this.price = price;
    }

    /**
     * Used to convert this pojo into a csv representation.
     * @return
     */
    @Override
    public String toString() {
        return parsedDate + "," + productName + "," + currency + "," + price;
    }
}

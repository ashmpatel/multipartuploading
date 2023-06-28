package com.ash.multipart.model;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Hold the trade data, simple pojo.
 */
@Slf4j
public class Trade {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String parsedDate;
    private Long productId;
    private String currency;
    private BigDecimal price;

    /**
     * When trade data is sent per line, parses it , esp the date then stores it,
     * @param text
     */
    public Trade(String text) {
        if (text.length() > 0) {
            String[] values = text.split(",");
            this.parsedDate = parseDate(values[0], text);
            this.productId = Long.parseLong(values[1]);
            this.currency = values[2];
            if (values[3].indexOf(".") == -1) {
                values[3] = values[3].trim() + ".0";
            }
            ;
            this.price = new BigDecimal(values[3].trim());
            log.info("Processed trade data :" + text);
        }
    }


    private String parseDate(String textDate, String tradeRow) {
        LocalDate parsedDate = null;
        try {
            parsedDate = LocalDate.parse(textDate, formatter);
        } catch (DateTimeParseException e) {
            log.error("Trade has invalid date " + tradeRow);
            log.error("The date " + textDate + " could not be parsed");
            e.printStackTrace();
        }
        String formattedDate = parsedDate.format(formatter);
        return formattedDate;
    }

    public String getParsedDate() {
        return parsedDate;
    }

    public Long getProductId() {
        return productId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getPrice() {
        return price;
    }
}

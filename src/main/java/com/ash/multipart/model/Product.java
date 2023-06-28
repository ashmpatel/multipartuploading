package com.ash.multipart.model;

/**
 * To hold the product csv data.
 */
public class Product {

    private long productId;
    private String productName;

    private static final String CSV_SEPARATOR = ",";

    public Product(String productRow) {
        if (productRow.length() > 0) {
            String[] values = productRow.split(CSV_SEPARATOR);
            this.productId = Long.parseLong(values[0]);
            this.productName = values[1];

        }
    }

    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                '}';
    }
}

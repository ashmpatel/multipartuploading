package com.ash.multipart.utils;

import com.ash.multipart.listener.CallBackListener;
import com.ash.multipart.model.Product;

import java.util.HashMap;
import java.util.Map;

/**
 * Callback listener to process Products
 */
public class ListenForProductData implements CallBackListener {

    private static Map productMap = null;
    static {
        productMap = new HashMap<Long, Product>();
    }

    @Override
    public Product callBack(String data) {
        Product prd = new Product(data);
        productMap.put(prd.getProductId(),prd);
        return prd;
    }

    public Map getProducts() {
        Map<Long, Product> shallowCopy = new HashMap<>();
        shallowCopy.putAll(productMap);
        return shallowCopy;
    }

}

package com.ash.multipart.utils;

import com.ash.multipart.listener.CallBackListener;
import com.ash.multipart.model.EnrichedTradePayload;
import com.ash.multipart.model.Product;
import com.ash.multipart.model.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is the callback that gets called by the mem mapped file reader each time a row is successfully read.
 */
public class ProcessTradeData implements CallBackListener {

    private Map<Long, Product> productMap = Collections.emptyMap();
    private List<String> results;
    private List<EnrichedTradePayload> processed = new ArrayList<>();

    private static final String PRODUCT_LOOKUP_ERROR = "Missing Product Name";

    public ProcessTradeData(Map productMap , List results) {
        this.productMap = productMap;
        this.results = results;
    }

    /**
     * Processed the trade data by looking up the product id->product name in the map supplied.
     * On a loookup miss, a default value for Product Name is supplied as per the requirement.
     * @return
     */
    public List<EnrichedTradePayload> process() {
        for (String data: results) {
            Trade parsedTrade = new Trade(data);
            Product prd = productMap.get(parsedTrade.getProductId());
            String productName = "";

            // lookup the product Id and IF NOT in the map ,then as per the spec , return the string : Missing Product Name
            if (prd == null) {
                productName = PRODUCT_LOOKUP_ERROR;
            } else {
                productName = prd.getProductName();
            }

            // create the Enriched Trade Payload and add it to the result set
            // NOTE: I could out a Stream on this so the whole dataset is not stored in memory.
            EnrichedTradePayload enrichedTrade = new EnrichedTradePayload(parsedTrade.getParsedDate().trim(), productName.trim(), parsedTrade.getCurrency().trim(), parsedTrade.getPrice());
            processed.add(enrichedTrade);
        }
        return processed;
    }

    @Override
    public EnrichedTradePayload callBack(String data) {
       Trade parsedTrade = new Trade(data);
       Product prd = productMap.get(parsedTrade.getProductId());
       String productName="";

       // lookup the product Id and IF NOT in the map ,then as per the spec , return the string : Missing Product Name
       if (prd==null) {
           productName = PRODUCT_LOOKUP_ERROR;
       } else {
           productName = prd.getProductName();
        }

        // create the Enriched Trade Payload and add it to the result set
        // NOTE: I could out a Stream on this so the whole dataset is not stored in memory.
        EnrichedTradePayload enrichedTrade=  new EnrichedTradePayload(parsedTrade.getParsedDate(),productName, parsedTrade.getCurrency(),parsedTrade.getPrice());
        processed.add(enrichedTrade);
        return enrichedTrade;
    }

    public List getResults() {
        return processed;
    }

}

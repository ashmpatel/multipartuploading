package com.ash.multipart.utils;

import com.ash.multipart.model.EnrichedTradePayload;
import com.ash.multipart.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TradeProcessor {

    // the header for the return results. We need to know the column names for the csv file returned
    private final static String HEADER = "date,product_name,currency,price";
    private final static String PRODUCT_LOOKUP_DATA = "classpath:products.csv";

    //TODO: Read the productmap once in a separate bean and use that here.

    /**
     * Accepts the streamign tradedata from the Controller, joins it to the ProducrMap for the product_id lookup and
     * then sends the results in csv format back to the controller.
     * @param tradeData
     * @return
     * @throws IOException
     */
    public List<String> processTrades(Mono<List<String>> tradeData) throws IOException {
        // I do Not need to read this lookup file each time for a request. I am showing that in case it changes
        // between requests, we can read it and process as standard.
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);

        // read the product lookup data using a memory mapped small buffer processing.
        // This is so that in case it is large, we do not cause a lot of GC and use very small buffer, at most the length of 1 row of csv data is needed.
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();

        // hold the results per part file processed
        List<String> csvResults = new ArrayList<>();
        csvResults.add(HEADER);
        // Use a Reactive consumer to send results back as the data comes in
        tradeData.subscribe(data -> {
            ProcessTradeData temp = new ProcessTradeData(productMap, data);
            List<EnrichedTradePayload> res = temp.process();
            for (EnrichedTradePayload t : res) {
                csvResults.add(t.toString());
            }
        });

        return csvResults;

    }

}
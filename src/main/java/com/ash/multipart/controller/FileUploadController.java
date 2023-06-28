package com.ash.multipart.controller;

import com.ash.multipart.model.EnrichedTradePayload;
import com.ash.multipart.model.Product;
import com.ash.multipart.service.FileUploadService;
import com.ash.multipart.utils.ListenForProductData;
import com.ash.multipart.utils.ProcessTradeDate;
import com.ash.multipart.utils.ReadMemoryMappedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller that listens for file uploading of Trade data in csv format.
 * Looks up the product id and maps it to a name if id is available in the products lookup file.
 */
@RestController
@RequestMapping("api/v1")
public class FileUploadController {

    // the header for the return results. We need to know the column names for the csv file returned
    private final static String HEADER= "date,product_name,currency,price";
    private final static String NEWLINE_SEPARATOR= "\n";
    private final static String PRODUCT_LOOKUP_DATA= "classpath:products.csv";

    @Autowired
    FileUploadService fileUploadService;

    /**
     * Processes the data as it streams in.
     * @param filePart
     * @return Csv data in String format.
     * @throws IOException
     */
    @PostMapping(value = "/enrich", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/csv")
    @ResponseStatus(value = HttpStatus.OK)
    public String upload(@RequestPart("file") FilePart filePart) throws IOException {

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

        // Process the Trade csv file. Get the results added to in the callback by the main csv reader.
        // NOTE: we do Not store the data on disk at any point, we only process it in the buffer.
        Mono<List<String>> tradeData = fileUploadService.getLines(filePart).collectList();

        // Use a Reactive consumer to send results back as the data comes in
        tradeData.subscribe(data -> {
            ProcessTradeDate temp = new ProcessTradeDate(productMap, data);
            List<EnrichedTradePayload> res = temp.process();
            for(EnrichedTradePayload t: res) {
                csvResults.add(t.toString());
            }
         });

        // return the results as the data comes in and is processed
        return String.join(NEWLINE_SEPARATOR, csvResults);

    }
}

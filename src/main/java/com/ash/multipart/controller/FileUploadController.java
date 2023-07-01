package com.ash.multipart.controller;

import com.ash.multipart.service.FileUploadService;
import com.ash.multipart.utils.TradeProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

/**
 * Controller that listens for file uploading of Trade data in csv format.
 * Looks up the product id and maps it to a name if id is available in the products lookup file.
 */
@RestController
@RequestMapping("api/v1")
public class FileUploadController {

    private final static String NEWLINE_SEPARATOR = "\n";

    @Autowired
    TradeProcessor tradeProcessor;

    @Autowired
    FileUploadService fileUploadService;

    /**
     * Processes the data as it streams in.
     *
     * @param filePart
     * @return Csv data in String format.
     * @throws IOException
     */
    @PostMapping(value = "/enrich", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/csv")
    @ResponseStatus(value = HttpStatus.OK)
    public String upload(@RequestPart("file") FilePart filePart) throws IOException {

        // Process the Trade csv file. Get the results added to in the callback by the main csv reader.
        // NOTE: we do Not store the data on disk at any point, we only process it in the buffer.
        Mono<List<String>> tradeData = fileUploadService.getLines(filePart).collectList();

        List<String> tradeResults = tradeProcessor.processTrades(tradeData);

        // return the results as the data comes in and is processed
        return String.join(NEWLINE_SEPARATOR, tradeResults);

    }


}

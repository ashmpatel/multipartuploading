package com.ash.multipart;

import com.ash.multipart.model.EnrichedTradePayload;
import com.ash.multipart.model.Product;
import com.ash.multipart.utils.ListenForProductData;
import com.ash.multipart.utils.ProcessTradeData;
import com.ash.multipart.utils.ReadMemoryMappedFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MultipartFileUploadApplicationTests {

    private static final Logger LOGGER = LogManager.getLogger(MultipartFileUploadApplicationTests.class.getName());

    private final static String PRODUCT_LOOKUP_DATA= "classpath:products.csv";

    private int port = 8080;

    WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080" + this.port)
                .build();
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("/products.csv", MultipartFileUploadApplicationTests.class));
        return builder.build();
    }


   @Test
    public void producrsReadAndCountIsCorrect() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();
        assert(productMap.size()==10);
    }

    @Test
    public void productsReadAndAllEntriesExist() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();
        Long key= Long.valueOf(1);
        Product test1= productMap.get(key);
        assert(test1.getProductName().trim().equals("Treasury Bills Domestic"));
    }

    @Test
    public void processTradeDateCheckEnrichedTradeDataIsCorrect() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();

        String data = "20160101,1,EUR,10";
        String data2 ="20160101,2,EUR,20.1";
        List<String> fileData= new ArrayList<>();
        fileData.add(data);
        fileData.add(data2);
        ProcessTradeData test = new ProcessTradeData(productMap, fileData);
        List<EnrichedTradePayload> t = test.process();
        EnrichedTradePayload first = t.get(0);
        assert(first.toString().equals("20160101,Treasury Bills Domestic,EUR,10.0"));
    }

    @Test
    public void processTradeDataWithDateIssue() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();
        boolean caughtError = false;

        String data = "20165590,1,EUR,10";
        String data2 ="20160101,2,EUR,20.1";
        List<String> fileData= new ArrayList<>();
        fileData.add(data);
        fileData.add(data2);
        ProcessTradeData test = new ProcessTradeData(productMap, fileData);
        // this will cause a null pointer as the date can not be parsed as its invalid to there is no date object
        // so I expect a NullPointerException
        try {
            List<EnrichedTradePayload> t = test.process();
        }
        catch(NullPointerException e) {
            caughtError = true;
        }
        assert(caughtError==true);
    }


    @Test
    public void processTradeDateWhenProductIdIsNotInProductsList() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadMemoryMappedFile allProducts = new ReadMemoryMappedFile(Path.of(file.getPath()), productListener);
        allProducts.processFile();

        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();

        String data = "20160101,999,EUR,10";
        List<String> fileData= new ArrayList<>();
        fileData.add(data);
        ProcessTradeData test = new ProcessTradeData(productMap, fileData);
        List<EnrichedTradePayload> t = test.process();
        EnrichedTradePayload first = t.get(0);
        // the product id was not in the product map so I expect Missing Product Name in the trade processing BUT all other values
        // should be present
        assert(first.toString().equals("20160101,Missing Product Name,EUR,10.0"));
    }

    //TODO: Add test for empty tradefile
    //TODO: Add test for empty product map
    //TODO: Add test for malformed trade file
    //TODO: Add test for malformed product file
    //TODO: Split trade data into chunks and process chunk sin threads for performance
}

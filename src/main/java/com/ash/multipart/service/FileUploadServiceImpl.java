package com.ash.multipart.service;

import com.ash.multipart.utils.MultipartFileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Processes the parts of file data as it streams in.
 * At no point do I save the data to disk. It is processed all in the buffer for the part of the data that has come in.
 * Makes more efficient use of mem for large files uploads.
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * This is for single file upload
     * @param filePart
     * @return
     */
    @Override
    public Flux<String> getLines(FilePart filePart) {
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .map(this::processAndGetLinesAsList)
                .flatMapIterable(Function.identity());
    }

    /**
     * This is a step to show we can filter the data as it streams in. The requirement did not ask for it, but
     * I show how the design can deal with it, if needed.
     * @param string
     * @return
     */
    private List<String> processAndGetLinesAsList(String string) {

        Supplier<Stream<String>> streamSupplier = string::lines;

        /* can use this in the return to see if the data parses properly per line but for now, I am NTO using this
         as the requirement is to log the error and return "Missing Product Name" when the id look up fails
         This is just to show, we can filter bad data out as load time
         */
        var isFileOk = streamSupplier.get().allMatch(line -> line.matches(MultipartFileUploadUtils.REGEX_RULES));

        // can use isFileOk instead of True IF we want to check the data format as it streams in.
        return true ? streamSupplier.get().filter(s -> !s.isBlank()).collect(Collectors.toList()) : new ArrayList<>();
    }
}

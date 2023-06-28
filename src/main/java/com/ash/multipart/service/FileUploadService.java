package com.ash.multipart.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Interface for the FileUploadService that processes part of the file as it gets uploaded.
 * Converts the data to lines of string from bytes.
 */
@Component
public interface FileUploadService {
    // this is for single file upload.
    Flux<String> getLines(FilePart filePart);

}

package com.ash.multipart.utils;

import com.ash.multipart.listener.CallBackListener;
import com.ash.multipart.model.EnrichedTradePayload;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * Reads a file using memory mapping.
 * I have sized the buffer to be 1meg but you can tune this for the server this gets deployed on.
 */
public class ReadMemoryMappedFile {

    // you can size this as small as 5 bytes or as large as you like and the code will work correctly
    //so read into a buffer og 20 bytes or read into a buffer of 5 meg, it will still read the data correctly
    private static final long MAP_SIZE = 1 * 1024 * 1024; // 1 MB in bytes
    private static final byte NEWLINE = '\n';

    //Size needs to be big enough to hold atleast one line of csv data row
    private static final int ONE_LINE_BUFFER_SIZE = 200;

    private CallBackListener callWithData;
    private Path dataPath;
    private Stream<EnrichedTradePayload> t;

    public ReadMemoryMappedFile(Path filePath, CallBackListener callWithData) {
        this.dataPath = filePath;
        this.callWithData = callWithData;
    }

    public void processFile() throws IOException {

        if (dataPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        try (FileChannel fileChannel = FileChannel.open(dataPath, StandardOpenOption.READ)) {
            int count = 0;
            long position = 0;
            long length = fileChannel.size();
            long endPos = 0;
            int startArray = 0;

            // needs to be big enough to fit ONE line of the data
            byte data[] = new byte[ONE_LINE_BUFFER_SIZE];

            // while we have data, keep processing
            while (position < length) {

                long remaining = length - position;
                long bytestomap = Math.min(MAP_SIZE, remaining);

                /*
                 I read the file into a memory mapped area but I could have used buffer.flip() and buffer.compact().
                 It's a choice I made as most machines have atleast some memory spare so a small memory buffer is affordable
                */
                MappedByteBuffer mbBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, bytestomap);

                while (mbBuffer.hasRemaining()) {

                    byte dataRead = mbBuffer.get();

                    data[count] = dataRead;
                    count++;
                    endPos++;
                    startArray++;

                    if (dataRead == NEWLINE) {
                        char[] slicedArray = new char[startArray - 1];
                        for (int i = 0; i < slicedArray.length; i++) {
                            slicedArray[i] = (char) data[i];
                        }
                        // can pass the buffer and the start and end pos IF we want to avoid a copy and GC then
                        // do not make this string, pass the data buffer
                        String dataFromFile = new String(slicedArray);
                        callWithData.callBack(dataFromFile);

                        startArray = 0;
                        count = 0;
                    }

                }

                // If you want to see the pointers in the file as we read it, use this for debugging
                //System.out.println("Read from  " + position + " to " + (position+endPos));
                position = position + endPos;
                if (position >= length) {
                    int sizeOfString = count;
                    char[] slicedArray = new char[sizeOfString];
                    for (int i = 0; i < slicedArray.length; i++) {
                        slicedArray[i] = (char) data[i];
                    }
                    String dataFromFile = new String(slicedArray);
                    callWithData.callBack(dataFromFile);
                }
                endPos = 0;
            }
        }
    }

}

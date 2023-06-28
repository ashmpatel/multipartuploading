package com.ash.multipart.listener;

// Callback used by the memory mapped file reader
public interface CallBackListener<T> {

    T callBack(String data);
}

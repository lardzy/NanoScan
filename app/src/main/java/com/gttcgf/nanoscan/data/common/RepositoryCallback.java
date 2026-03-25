package com.gttcgf.nanoscan.data.common;

public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}

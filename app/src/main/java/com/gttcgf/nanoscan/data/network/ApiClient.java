package com.gttcgf.nanoscan.data.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public final class ApiClient {
    public static final String BASE_URL = "https://newnirtechnolgy.top/api";
    private static OkHttpClient sharedClient;

    private ApiClient() {
    }

    public static synchronized OkHttpClient getClient() {
        if (sharedClient == null) {
            sharedClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return sharedClient;
    }
}

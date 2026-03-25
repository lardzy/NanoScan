package com.gttcgf.nanoscan.sdk;

public class ScanFailure {
    private final String title;
    private final String message;

    public ScanFailure(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}

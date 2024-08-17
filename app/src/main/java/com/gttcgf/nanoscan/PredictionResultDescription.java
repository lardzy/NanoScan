package com.gttcgf.nanoscan;

import java.io.Serializable;

public class PredictionResultDescription implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String dateTime, predictResult;

    public PredictionResultDescription(String dateTime, String predictResult) {
        this.dateTime = dateTime;
        this.predictResult = predictResult;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getPredictResult() {
        return predictResult;
    }
}

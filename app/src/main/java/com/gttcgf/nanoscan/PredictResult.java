package com.gttcgf.nanoscan;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PredictResult implements Comparable<PredictResult>, Serializable {
    private static final long serialVersionUID = 1L;
    private final String material;
    private final float percentage;
    private final String predictSessionUUID;

    public PredictResult(String material, float percentage, String predictSessionUUID) {
        this.material = material;
        this.percentage = percentage;
        this.predictSessionUUID = predictSessionUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictResult that = (PredictResult) o;
        return Float.compare(percentage, that.percentage) == 0 && Objects.equals(material, that.material) && Objects.equals(predictSessionUUID, that.predictSessionUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, percentage, predictSessionUUID);
    }

    @Override
    public int compareTo(PredictResult predictResult) {
        return Float.compare(this.percentage, predictResult.percentage);
    }

    @NonNull
    @Override
    public String toString() {
        return "PredictResult{" +
                "material='" + material + '\'' +
                ", percentage=" + percentage +
                ", predictSessionUUID='" + predictSessionUUID + '\'' +
                '}';
    }
}

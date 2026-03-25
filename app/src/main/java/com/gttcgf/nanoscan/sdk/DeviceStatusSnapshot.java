package com.gttcgf.nanoscan.sdk;

public class DeviceStatusSnapshot {
    private final int battery;
    private final float temperature;
    private final float humidity;
    private final String totalLampTime;
    private final String referenceUpdateTime;

    public DeviceStatusSnapshot(int battery, float temperature, float humidity, String totalLampTime, String referenceUpdateTime) {
        this.battery = battery;
        this.temperature = temperature;
        this.humidity = humidity;
        this.totalLampTime = totalLampTime;
        this.referenceUpdateTime = referenceUpdateTime;
    }

    public int getBattery() {
        return battery;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public String getTotalLampTime() {
        return totalLampTime;
    }

    public String getReferenceUpdateTime() {
        return referenceUpdateTime;
    }
}

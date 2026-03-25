package com.gttcgf.nanoscan.data.model;

public class DeviceLocalStatus {
    private final int battery;
    private final String totalLampTime;
    private final String referenceUpdateTime;

    public DeviceLocalStatus(int battery, String totalLampTime, String referenceUpdateTime) {
        this.battery = battery;
        this.totalLampTime = totalLampTime;
        this.referenceUpdateTime = referenceUpdateTime;
    }

    public int getBattery() {
        return battery;
    }

    public String getTotalLampTime() {
        return totalLampTime;
    }

    public String getReferenceUpdateTime() {
        return referenceUpdateTime;
    }
}

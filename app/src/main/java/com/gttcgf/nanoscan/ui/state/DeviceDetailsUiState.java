package com.gttcgf.nanoscan.ui.state;

import com.gttcgf.nanoscan.DeviceItem;

public class DeviceDetailsUiState {
    private final boolean loading;
    private final DeviceItem deviceItem;
    private final int battery;
    private final String totalLampTime;
    private final String referenceUpdateDate;
    private final int spectraCount;
    private final boolean warmUp;

    public DeviceDetailsUiState(boolean loading, DeviceItem deviceItem, int battery, String totalLampTime, String referenceUpdateDate, int spectraCount, boolean warmUp) {
        this.loading = loading;
        this.deviceItem = deviceItem;
        this.battery = battery;
        this.totalLampTime = totalLampTime;
        this.referenceUpdateDate = referenceUpdateDate;
        this.spectraCount = spectraCount;
        this.warmUp = warmUp;
    }

    public static DeviceDetailsUiState initial(DeviceItem deviceItem) {
        return new DeviceDetailsUiState(true, deviceItem, -1, "-", "-", 0, false);
    }

    public boolean isLoading() {
        return loading;
    }

    public DeviceItem getDeviceItem() {
        return deviceItem;
    }

    public int getBattery() {
        return battery;
    }

    public String getTotalLampTime() {
        return totalLampTime;
    }

    public String getReferenceUpdateDate() {
        return referenceUpdateDate;
    }

    public int getSpectraCount() {
        return spectraCount;
    }

    public boolean hasSpectra() {
        return spectraCount > 0;
    }

    public boolean isWarmUp() {
        return warmUp;
    }
}

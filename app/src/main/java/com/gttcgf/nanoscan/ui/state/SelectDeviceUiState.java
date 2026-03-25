package com.gttcgf.nanoscan.ui.state;

import com.ISCSDK.ISCNIRScanSDK;

import java.util.ArrayList;
import java.util.List;

public class SelectDeviceUiState {
    private final boolean scanning;
    private final boolean loading;
    private final List<ISCNIRScanSDK.NanoDevice> devices;

    public SelectDeviceUiState(boolean scanning, boolean loading, List<ISCNIRScanSDK.NanoDevice> devices) {
        this.scanning = scanning;
        this.loading = loading;
        this.devices = new ArrayList<>(devices);
    }

    public static SelectDeviceUiState initial() {
        return new SelectDeviceUiState(true, true, new ArrayList<>());
    }

    public boolean isScanning() {
        return scanning;
    }

    public boolean isLoading() {
        return loading;
    }

    public List<ISCNIRScanSDK.NanoDevice> getDevices() {
        return new ArrayList<>(devices);
    }

    public boolean isEmpty() {
        return devices.isEmpty();
    }
}

package com.gttcgf.nanoscan.ui.state;

import com.gttcgf.nanoscan.DeviceItem;

import java.util.ArrayList;
import java.util.List;

public class MainUiState {
    private final boolean loading;
    private final boolean interactionsEnabled;
    private final List<DeviceItem> devices;
    private final boolean empty;

    public MainUiState(boolean loading, boolean interactionsEnabled, List<DeviceItem> devices) {
        this.loading = loading;
        this.interactionsEnabled = interactionsEnabled;
        this.devices = new ArrayList<>(devices);
        this.empty = devices.isEmpty();
    }

    public static MainUiState idle() {
        return new MainUiState(true, false, new ArrayList<>());
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isInteractionsEnabled() {
        return interactionsEnabled;
    }

    public List<DeviceItem> getDevices() {
        return new ArrayList<>(devices);
    }

    public boolean isEmpty() {
        return empty;
    }
}

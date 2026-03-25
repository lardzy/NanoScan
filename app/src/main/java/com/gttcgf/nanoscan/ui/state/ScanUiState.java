package com.gttcgf.nanoscan.ui.state;

import com.gttcgf.nanoscan.PredictResult;
import com.gttcgf.nanoscan.sdk.ConnectionStage;
import com.gttcgf.nanoscan.sdk.PredictionState;
import com.gttcgf.nanoscan.sdk.SaveState;
import com.gttcgf.nanoscan.sdk.ScanMode;

import java.util.ArrayList;
import java.util.List;

public class ScanUiState {
    private final ConnectionStage connectionStage;
    private final ScanMode scanMode;
    private final boolean useFactoryReference;
    private final boolean scanning;
    private final PredictionState predictionState;
    private final SaveState saveState;
    private final List<PredictResult> predictResults;
    private final String batteryText;
    private final String updateTimeText;

    public ScanUiState(ConnectionStage connectionStage,
                       ScanMode scanMode,
                       boolean useFactoryReference,
                       boolean scanning,
                       PredictionState predictionState,
                       SaveState saveState,
                       List<PredictResult> predictResults,
                       String batteryText,
                       String updateTimeText) {
        this.connectionStage = connectionStage;
        this.scanMode = scanMode;
        this.useFactoryReference = useFactoryReference;
        this.scanning = scanning;
        this.predictionState = predictionState;
        this.saveState = saveState;
        this.predictResults = new ArrayList<>(predictResults);
        this.batteryText = batteryText;
        this.updateTimeText = updateTimeText;
    }

    public static ScanUiState initial() {
        return new ScanUiState(ConnectionStage.IDLE, ScanMode.SCAN_AND_PREDICT, true, false, PredictionState.IDLE, SaveState.IDLE, new ArrayList<>(), "N/A", "-");
    }

    public ConnectionStage getConnectionStage() {
        return connectionStage;
    }

    public ScanMode getScanMode() {
        return scanMode;
    }

    public boolean isUseFactoryReference() {
        return useFactoryReference;
    }

    public boolean isScanning() {
        return scanning;
    }

    public PredictionState getPredictionState() {
        return predictionState;
    }

    public SaveState getSaveState() {
        return saveState;
    }

    public List<PredictResult> getPredictResults() {
        return new ArrayList<>(predictResults);
    }

    public String getBatteryText() {
        return batteryText;
    }

    public String getUpdateTimeText() {
        return updateTimeText;
    }
}

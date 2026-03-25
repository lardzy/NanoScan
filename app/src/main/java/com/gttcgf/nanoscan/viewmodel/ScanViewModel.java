package com.gttcgf.nanoscan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gttcgf.nanoscan.PredictResult;
import com.gttcgf.nanoscan.sdk.ConnectionStage;
import com.gttcgf.nanoscan.sdk.PredictionState;
import com.gttcgf.nanoscan.sdk.SaveState;
import com.gttcgf.nanoscan.sdk.ScanMode;
import com.gttcgf.nanoscan.ui.common.UiEvent;
import com.gttcgf.nanoscan.ui.state.ScanUiState;

import java.util.ArrayList;
import java.util.List;

public class ScanViewModel extends AndroidViewModel {
    public static class ScanAction {
        private final String title;
        private final String message;

        public ScanAction(String title, String message) {
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

    private final MutableLiveData<ScanUiState> uiState = new MutableLiveData<>(ScanUiState.initial());
    private final MutableLiveData<UiEvent<ScanAction>> events = new MutableLiveData<>();

    public ScanViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ScanUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<ScanAction>> getEvents() {
        return events;
    }

    public void setConnectionStage(ConnectionStage connectionStage) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(connectionStage, state.getScanMode(), state.isUseFactoryReference(), state.isScanning(), state.getPredictionState(), state.getSaveState(), state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void setScanMode(ScanMode scanMode) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), scanMode, state.isUseFactoryReference(), state.isScanning(), state.getPredictionState(), state.getSaveState(), state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void setUseFactoryReference(boolean useFactoryReference) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), state.getScanMode(), useFactoryReference, state.isScanning(), state.getPredictionState(), state.getSaveState(), state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void setScanning(boolean scanning) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), state.getScanMode(), state.isUseFactoryReference(), scanning, state.getPredictionState(), state.getSaveState(), state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void setPredictionState(PredictionState predictionState, List<PredictResult> predictResults) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), state.getScanMode(), state.isUseFactoryReference(), state.isScanning(), predictionState, state.getSaveState(), predictResults != null ? predictResults : state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void setSaveState(SaveState saveState) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), state.getScanMode(), state.isUseFactoryReference(), state.isScanning(), state.getPredictionState(), saveState, state.getPredictResults(), state.getBatteryText(), state.getUpdateTimeText()));
    }

    public void updateDeviceStatusText(String batteryText, String updateTimeText) {
        ScanUiState state = requireState();
        uiState.postValue(new ScanUiState(state.getConnectionStage(), state.getScanMode(), state.isUseFactoryReference(), state.isScanning(), state.getPredictionState(), state.getSaveState(), state.getPredictResults(), batteryText, updateTimeText));
    }

    public void showError(String title, String message) {
        events.postValue(new UiEvent<>(new ScanAction(title, message)));
    }

    private ScanUiState requireState() {
        ScanUiState state = uiState.getValue();
        return state != null ? state : ScanUiState.initial();
    }
}

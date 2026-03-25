package com.gttcgf.nanoscan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.data.model.DeviceLocalStatus;
import com.gttcgf.nanoscan.data.model.UserSession;
import com.gttcgf.nanoscan.data.repository.DeviceRepository;
import com.gttcgf.nanoscan.data.repository.SpectralRepository;
import com.gttcgf.nanoscan.data.repository.UserSessionRepository;
import com.gttcgf.nanoscan.ui.common.UiEvent;
import com.gttcgf.nanoscan.ui.state.DeviceDetailsUiState;

import java.util.LinkedHashMap;

public class DeviceDetailsViewModel extends AndroidViewModel {
    public static class DeviceDetailsAction {
        public enum Type {
            SHOW_TOAST,
            OPEN_SCAN,
            OPEN_SPECTRUM_PREVIEW,
            FINISH
        }

        private final Type type;
        private final String message;
        private final String fileName;
        private final DeviceItem deviceItem;
        private final boolean warmUp;

        public DeviceDetailsAction(Type type, String message, String fileName, DeviceItem deviceItem, boolean warmUp) {
            this.type = type;
            this.message = message;
            this.fileName = fileName;
            this.deviceItem = deviceItem;
            this.warmUp = warmUp;
        }

        public Type getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public String getFileName() {
            return fileName;
        }

        public DeviceItem getDeviceItem() {
            return deviceItem;
        }

        public boolean isWarmUp() {
            return warmUp;
        }
    }

    private final MutableLiveData<DeviceDetailsUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<DeviceDetailsAction>> events = new MutableLiveData<>();
    private final UserSessionRepository userSessionRepository;
    private final DeviceRepository deviceRepository;
    private final SpectralRepository spectralRepository;
    private DeviceItem currentDevice;
    private String userPhoneNumber = "";

    public DeviceDetailsViewModel(@NonNull Application application) {
        super(application);
        this.userSessionRepository = new UserSessionRepository(application);
        this.deviceRepository = new DeviceRepository(application);
        this.spectralRepository = new SpectralRepository(application);
    }

    public LiveData<DeviceDetailsUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<DeviceDetailsAction>> getEvents() {
        return events;
    }

    public void initialize(DeviceItem deviceItem) {
        this.currentDevice = deviceItem;
        UserSession session = userSessionRepository.getCurrentSession();
        this.userPhoneNumber = session.getPhoneNumber();
        uiState.setValue(DeviceDetailsUiState.initial(deviceItem));
        refresh(false);
    }

    public void refresh(boolean keepWarmUp) {
        if (currentDevice == null) {
            return;
        }
        LinkedHashMap<String, com.gttcgf.nanoscan.PredictionResultDescription> spectralIndex = spectralRepository.loadSpectralIndex(userPhoneNumber, currentDevice.getDeviceMac());
        DeviceItem latestDevice = deviceRepository.findDevice(userPhoneNumber, currentDevice.getDeviceMac());
        if (latestDevice != null) {
            currentDevice = latestDevice;
        }
        DeviceLocalStatus deviceStatus = deviceRepository.readDeviceLocalStatus(currentDevice.getDeviceMac());
        uiState.postValue(new DeviceDetailsUiState(false, currentDevice, deviceStatus.getBattery(), deviceStatus.getTotalLampTime(), deviceStatus.getReferenceUpdateTime(), spectralIndex.size(), keepWarmUp && uiState.getValue() != null && uiState.getValue().isWarmUp()));
    }

    public void updateWarmUp(boolean warmUp) {
        DeviceDetailsUiState state = uiState.getValue();
        if (state == null) {
            return;
        }
        uiState.setValue(new DeviceDetailsUiState(state.isLoading(), state.getDeviceItem(), state.getBattery(), state.getTotalLampTime(), state.getReferenceUpdateDate(), state.getSpectraCount(), warmUp));
    }

    public void openScan() {
        DeviceDetailsUiState state = uiState.getValue();
        if (state == null) {
            return;
        }
        events.setValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.OPEN_SCAN, null, null, state.getDeviceItem(), state.isWarmUp())));
    }

    public void openSpectrumPreview(String fileName) {
        events.setValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.OPEN_SPECTRUM_PREVIEW, null, fileName, null, false)));
    }

    public boolean deleteSpectrum(String fileName) {
        boolean deleted = spectralRepository.deleteSpectralRecord(userPhoneNumber, currentDevice.getDeviceMac(), fileName);
        if (deleted) {
            refresh(uiState.getValue() != null && uiState.getValue().isWarmUp());
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, fileName + " 删除成功！", null, null, false)));
        }
        return deleted;
    }

    public boolean deleteReference() {
        boolean deleted = spectralRepository.deleteReferenceIntensity(currentDevice.getDeviceMac());
        if (deleted) {
            refresh(uiState.getValue() != null && uiState.getValue().isWarmUp());
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "本地参比已成功删除！", null, null, false)));
        } else {
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "本地参比删除失败！", null, null, false)));
        }
        return deleted;
    }

    public boolean renameDevice(String newName) {
        boolean updated = deviceRepository.updateDeviceName(userPhoneNumber, currentDevice, newName);
        if (updated) {
            refresh(uiState.getValue() != null && uiState.getValue().isWarmUp());
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "修改成功", null, null, false)));
        } else {
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "修改失败", null, null, false)));
        }
        return updated;
    }

    public boolean deleteCurrentDevice() {
        boolean deleted = deviceRepository.deleteDevice(userPhoneNumber, currentDevice);
        if (deleted) {
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "删除成功", null, null, false)));
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.FINISH, null, null, null, false)));
        } else {
            events.postValue(new UiEvent<>(new DeviceDetailsAction(DeviceDetailsAction.Type.SHOW_TOAST, "删除失败", null, null, false)));
        }
        return deleted;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }
}

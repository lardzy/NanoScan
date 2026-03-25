package com.gttcgf.nanoscan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ISCSDK.ISCNIRScanSDK;
import com.gttcgf.nanoscan.data.model.UserSession;
import com.gttcgf.nanoscan.data.repository.DeviceRepository;
import com.gttcgf.nanoscan.data.repository.UserSessionRepository;
import com.gttcgf.nanoscan.ui.common.UiEvent;
import com.gttcgf.nanoscan.ui.state.SelectDeviceUiState;

import java.util.ArrayList;
import java.util.List;

public class SelectDeviceViewModel extends AndroidViewModel {
    public static class SelectDeviceAction {
        public enum Type {
            SHOW_TIMEOUT_DIALOG,
            SHOW_TOAST,
            FINISH_WITH_RESULT
        }

        private final Type type;
        private final String deviceName;
        private final String macAddress;
        private final String token;
        private final String message;

        public SelectDeviceAction(Type type, String deviceName, String macAddress, String token, String message) {
            this.type = type;
            this.deviceName = deviceName;
            this.macAddress = macAddress;
            this.token = token;
            this.message = message;
        }

        public Type getType() {
            return type;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public String getToken() {
            return token;
        }

        public String getMessage() {
            return message;
        }
    }

    private final MutableLiveData<SelectDeviceUiState> uiState = new MutableLiveData<>(SelectDeviceUiState.initial());
    private final MutableLiveData<UiEvent<SelectDeviceAction>> events = new MutableLiveData<>();
    private final UserSessionRepository userSessionRepository;
    private final DeviceRepository deviceRepository;
    private final List<ISCNIRScanSDK.NanoDevice> nanoDevices = new ArrayList<>();

    public SelectDeviceViewModel(@NonNull Application application) {
        super(application);
        this.userSessionRepository = new UserSessionRepository(application);
        this.deviceRepository = new DeviceRepository(application);
    }

    public LiveData<SelectDeviceUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<SelectDeviceAction>> getEvents() {
        return events;
    }

    public UserSession getCurrentSession() {
        return userSessionRepository.getCurrentSession();
    }

    public void onScanStarted() {
        uiState.postValue(new SelectDeviceUiState(true, true, nanoDevices));
    }

    public void onScanFinished(boolean isStopped) {
        uiState.postValue(new SelectDeviceUiState(false, false, nanoDevices));
        if (nanoDevices.isEmpty() && !isStopped) {
            events.postValue(new UiEvent<>(new SelectDeviceAction(SelectDeviceAction.Type.SHOW_TIMEOUT_DIALOG, null, null, null, null)));
        }
    }

    public void onDeviceFound(ISCNIRScanSDK.NanoDevice nanoDevice) {
        boolean exists = false;
        for (int i = 0; i < nanoDevices.size(); i++) {
            if (nanoDevices.get(i).getNanoMac().equals(nanoDevice.getNanoMac())) {
                nanoDevices.set(i, nanoDevice);
                exists = true;
                break;
            }
        }
        if (!exists) {
            nanoDevices.add(nanoDevice);
        }
        uiState.postValue(new SelectDeviceUiState(true, nanoDevices.isEmpty(), nanoDevices));
    }

    public void onDeviceAuthorized(String deviceName, String macAddress, String token) {
        deviceRepository.savePreferredDeviceSelection(macAddress, deviceName);
        events.postValue(new UiEvent<>(new SelectDeviceAction(SelectDeviceAction.Type.FINISH_WITH_RESULT, deviceName, macAddress, token, null)));
    }

    public void showToast(String message) {
        events.postValue(new UiEvent<>(new SelectDeviceAction(SelectDeviceAction.Type.SHOW_TOAST, null, null, null, message)));
    }
}

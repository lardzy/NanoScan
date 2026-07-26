package com.gttcgf.nanoscan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.LoginActivity;
import com.gttcgf.nanoscan.data.model.UserSession;
import com.gttcgf.nanoscan.data.repository.DeviceRepository;
import com.gttcgf.nanoscan.data.repository.UserSessionRepository;
import com.gttcgf.nanoscan.ui.common.UiEvent;
import com.gttcgf.nanoscan.ui.state.MainUiState;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    public static class MainAction {
        public enum Type {
            OPEN_USER_AGREEMENT,
            OPEN_LOGIN,
            OPEN_DEVICE_DETAILS,
            SHOW_TOAST
        }

        private final Type type;
        private final DeviceItem deviceItem;
        private final String message;

        public MainAction(Type type, DeviceItem deviceItem, String message) {
            this.type = type;
            this.deviceItem = deviceItem;
            this.message = message;
        }

        public Type getType() {
            return type;
        }

        public DeviceItem getDeviceItem() {
            return deviceItem;
        }

        public String getMessage() {
            return message;
        }
    }

    private final MutableLiveData<MainUiState> uiState = new MutableLiveData<>(MainUiState.idle());
    private final MutableLiveData<UiEvent<MainAction>> events = new MutableLiveData<>();
    private final UserSessionRepository userSessionRepository;
    private final DeviceRepository deviceRepository;
    private List<DeviceItem> allDevices = new ArrayList<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.userSessionRepository = new UserSessionRepository(application);
        this.deviceRepository = new DeviceRepository(application);
    }

    public LiveData<MainUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<MainAction>> getEvents() {
        return events;
    }

    public void refresh() {
        UserSession session = userSessionRepository.getCurrentSession();
        if (session.isFirstRun() && !session.isUserAgreed()) {
            events.postValue(new UiEvent<>(new MainAction(MainAction.Type.OPEN_USER_AGREEMENT, null, null)));
            return;
        }
        allDevices = deviceRepository.getDevices(session.getPhoneNumber());
        uiState.postValue(new MainUiState(true, false, allDevices));
        if (userSessionRepository.isDeveloperBypassEnabled()) {
            LoginActivity.userLoggedIn = true;
            uiState.postValue(new MainUiState(false, true, allDevices));
            return;
        }
        String loginToken = session.getToken();
        if (LoginActivity.userLoggedIn && !loginToken.isEmpty()) {
            uiState.postValue(new MainUiState(false, true, allDevices));
            return;
        }
        if (!loginToken.isEmpty()) {
            userSessionRepository.verifyLoginToken(loginToken, new com.gttcgf.nanoscan.data.common.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    userSessionRepository.setUserToken(data);
                    LoginActivity.userLoggedIn = true;
                    uiState.postValue(new MainUiState(false, true, allDevices));
                }

                @Override
                public void onError(String message) {
                    userSessionRepository.clearUserToken();
                    LoginActivity.userLoggedIn = false;
                    if (message != null && !message.isEmpty()) {
                        events.postValue(new UiEvent<>(new MainAction(MainAction.Type.SHOW_TOAST, null, "用户未登录！")));
                    }
                    events.postValue(new UiEvent<>(new MainAction(MainAction.Type.OPEN_LOGIN, null, null)));
                }
            });
            return;
        }
        events.postValue(new UiEvent<>(new MainAction(MainAction.Type.OPEN_LOGIN, null, null)));
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            uiState.postValue(new MainUiState(false, true, allDevices));
            return;
        }
        List<DeviceItem> filtered = new ArrayList<>();
        for (DeviceItem item : allDevices) {
            if (item.getDeviceName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        uiState.postValue(new MainUiState(false, true, filtered));
    }

    public void onDeviceClicked(int position) {
        List<DeviceItem> visibleDevices = uiState.getValue() != null ? uiState.getValue().getDevices() : new ArrayList<>();
        if (position < 0 || position >= visibleDevices.size()) {
            return;
        }
        events.postValue(new UiEvent<>(new MainAction(MainAction.Type.OPEN_DEVICE_DETAILS, visibleDevices.get(position), null)));
    }
}

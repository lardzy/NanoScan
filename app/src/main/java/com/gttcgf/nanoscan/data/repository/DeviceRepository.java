package com.gttcgf.nanoscan.data.repository;

import static com.ISCSDK.ISCNIRScanSDK.storeStringPref;

import android.content.Context;
import android.content.SharedPreferences;

import com.ISCSDK.ISCNIRScanSDK;
import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.R;
import com.gttcgf.nanoscan.data.common.RepositoryCallback;
import com.gttcgf.nanoscan.data.model.DeviceLocalStatus;
import com.gttcgf.nanoscan.data.network.ApiClient;
import com.gttcgf.nanoscan.data.network.ApiErrorParser;
import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceRepository {
    private final Context appContext;

    public DeviceRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public List<DeviceItem> getDevices(String userPhoneNumber) {
        return new ArrayList<>(SpectralDataUtils.readDeviceListFromFile(appContext, userPhoneNumber));
    }

    public boolean saveDevices(String userPhoneNumber, List<DeviceItem> devices) {
        return SpectralDataUtils.writeDeviceListToFile(appContext, userPhoneNumber, devices);
    }

    public boolean updateDeviceName(String userPhoneNumber, DeviceItem target, String newName) {
        List<DeviceItem> devices = getDevices(userPhoneNumber);
        for (DeviceItem device : devices) {
            if (device.getDeviceMac().equals(target.getDeviceMac())) {
                device.setDeviceName(newName);
                return saveDevices(userPhoneNumber, devices);
            }
        }
        return false;
    }

    public boolean deleteDevice(String userPhoneNumber, DeviceItem target) {
        List<DeviceItem> devices = getDevices(userPhoneNumber);
        boolean removed = devices.removeIf(device -> device.getDeviceMac().equals(target.getDeviceMac()));
        return removed && saveDevices(userPhoneNumber, devices);
    }

    public DeviceItem findDevice(String userPhoneNumber, String deviceMac) {
        for (DeviceItem device : getDevices(userPhoneNumber)) {
            if (device.getDeviceMac().equals(deviceMac)) {
                return device;
            }
        }
        return null;
    }

    public DeviceLocalStatus readDeviceLocalStatus(String deviceMac) {
        SharedPreferences prefs = appContext.getSharedPreferences(deviceMac, Context.MODE_PRIVATE);
        return new DeviceLocalStatus(
                prefs.getInt(appContext.getString(R.string.pref_device_battery), -1),
                prefs.getString(appContext.getString(R.string.pref_device_totalLampTime), "-"),
                prefs.getString(appContext.getString(R.string.pref_app_reference_update_time), "-")
        );
    }

    public void saveDeviceLocalStatus(String deviceMac,
                                      int battery,
                                      float temperature,
                                      float humidity,
                                      String totalLampTime,
                                      String referenceUpdateTime) {
        SharedPreferences prefs = appContext.getSharedPreferences(deviceMac, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(appContext.getString(R.string.pref_device_battery), battery)
                .putFloat(appContext.getString(R.string.pref_device_temperature), temperature)
                .putFloat(appContext.getString(R.string.pref_device_humidity), humidity)
                .putString(appContext.getString(R.string.pref_device_totalLampTime), totalLampTime)
                .putString(appContext.getString(R.string.pref_app_reference_update_time), referenceUpdateTime)
                .apply();
    }

    public void savePreferredDeviceSelection(String deviceMac, String deviceName) {
        storeStringPref(appContext, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceMac);
        storeStringPref(appContext, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, deviceName);
    }

    public void authorizeDevice(String username, String password, String pcode, String mcode, String token, String checkCode, RepositoryCallback<String> callback) {
        MediaType jsonMediaType = MediaType.get("application/json");
        JSONObject userObject = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            userObject.put("username", username);
            userObject.put("password", password);
            userObject.put("deviceauthorizationcode", checkCode);
            userObject.put("pcode", pcode);
            userObject.put("mcode", mcode);
            jsonObject.put("user", userObject);
        } catch (JSONException exception) {
            callback.onError("本地信息异常，请重试");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/add_machine")
                .addHeader("Authorization", token)
                .post(RequestBody.create(jsonObject.toString(), jsonMediaType))
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("网络请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bodyString = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() && response.code() == 200 && !bodyString.isEmpty()) {
                    try {
                        JSONObject responseObject = new JSONObject(bodyString);
                        JSONObject responseUserObject = responseObject.getJSONObject("user");
                        callback.onSuccess(responseUserObject.getString("token"));
                    } catch (JSONException exception) {
                        callback.onError("服务器返回的数据格式错误，请稍后再试");
                    }
                    return;
                }
                callback.onError(ApiErrorParser.parse(bodyString));
            }
        });
    }
}

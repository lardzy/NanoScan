package com.gttcgf.nanoscan.data.repository;

import androidx.annotation.NonNull;

import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.PredictResult;
import com.gttcgf.nanoscan.R;
import com.gttcgf.nanoscan.data.common.RepositoryCallback;
import com.gttcgf.nanoscan.data.network.ApiClient;
import com.gttcgf.nanoscan.data.network.ApiErrorParser;
import com.gttcgf.nanoscan.tools.RSAEncrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PredictionRepository {
    private final android.content.Context appContext;

    public PredictionRepository(android.content.Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void requestPrediction(DeviceItem deviceItem, String predictSessionUUID, String csvPayload, RepositoryCallback<List<PredictResult>> callback) {
        String encryptedCode;
        try {
            encryptedCode = RSAEncrypt.encryptData(deviceItem.getDeviceMac(), RSAEncrypt.loadPublicKey(appContext, R.raw.p_key));
        } catch (Exception exception) {
            callback.onError("数据加密失败，请重新安装软件");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("test_code", encryptedCode);
            jsonObject.put("test_id", predictSessionUUID);
            jsonObject.put("indata", csvPayload);
        } catch (JSONException exception) {
            callback.onError("预测请求参数异常");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/test")
                .addHeader("Authorization", deviceItem.getDeviceToken())
                .post(RequestBody.create(jsonObject.toString(), MediaType.get("application/json")))
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bodyString = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() && response.code() == 200 && !bodyString.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(bodyString);
                        List<PredictResult> predictResults = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String[] parts = jsonArray.getString(i).split(":");
                            if (parts.length < 2) {
                                continue;
                            }
                            String material = parts[0];
                            String percentageString = parts[1].replace("%", "");
                            float percentage = Float.parseFloat(percentageString);
                            if (!material.isEmpty() && percentage > 0 && percentage <= 100) {
                                predictResults.add(new PredictResult(material, percentage, predictSessionUUID));
                            }
                        }
                        if (predictResults.isEmpty()) {
                            callback.onError("预测结果为空");
                            return;
                        }
                        predictResults.sort(Collections.reverseOrder());
                        callback.onSuccess(predictResults);
                    } catch (JSONException | NumberFormatException exception) {
                        callback.onError("返回结果解析异常，请重试");
                    }
                    return;
                }
                if (response.code() == 403) {
                    callback.onError("请在设备管理界面重新添加设备！\n" + ApiErrorParser.parse(bodyString));
                    return;
                }
                callback.onError(ApiErrorParser.parse(bodyString));
            }
        });
    }
}

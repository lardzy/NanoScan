package com.gttcgf.nanoscan.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.gttcgf.nanoscan.LoginActivity;
import com.gttcgf.nanoscan.R;
import com.gttcgf.nanoscan.data.common.RepositoryCallback;
import com.gttcgf.nanoscan.data.model.UserSession;
import com.gttcgf.nanoscan.data.network.ApiClient;
import com.gttcgf.nanoscan.data.network.ApiErrorParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserSessionRepository {
    private final Context appContext;
    private final SharedPreferences preferences;

    public UserSessionRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.preferences = appContext.getSharedPreferences("default", Context.MODE_PRIVATE);
    }

    public UserSession getCurrentSession() {
        return new UserSession(
                preferences.getString(appContext.getString(R.string.pref_user_phone_number), ""),
                preferences.getString(appContext.getString(R.string.pref_user_password), ""),
                preferences.getString(appContext.getString(R.string.pref_user_token), ""),
                preferences.getString(appContext.getString(R.string.pref_user_ipAddress), ""),
                preferences.getBoolean(appContext.getString(R.string.pref_first_run), true),
                preferences.getBoolean(appContext.getString(R.string.pref_user_agreed), false)
        );
    }

    public void setUserToken(String token) {
        preferences.edit().putString(appContext.getString(R.string.pref_user_token), token).apply();
    }

    public void clearUserToken() {
        preferences.edit().remove(appContext.getString(R.string.pref_user_token)).apply();
    }

    public void clearSavedPassword() {
        preferences.edit().putString(appContext.getString(R.string.pref_user_password), "").apply();
    }

    public void markUserLoggedOut() {
        LoginActivity.userLoggedIn = false;
        preferences.edit()
                .putString(appContext.getString(R.string.pref_user_token), "")
                .putString(appContext.getString(R.string.pref_user_password), "")
                .apply();
    }

    public void verifyLoginToken(String token, RepositoryCallback<String> callback) {
        RequestBody body = RequestBody.create(new byte[0], null);
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/users/login")
                .addHeader("Authorization", token)
                .post(body)
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
                        JSONObject jsonObject = new JSONObject(bodyString);
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        callback.onSuccess(userObject.getString("token"));
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

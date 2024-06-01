package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gttcgf.nanoscan.guidingSteps.IntroGuideActivity;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerificationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String serverUrl = "https://newnirtechnolgy.top/api";
    private static final String TAG = "LoginActivity";
    // todo: 调试用
    Button btn_debug;
    private EditText phone_number, password;
    private TextView forgot_password, register;
    private Button login_button;
    private boolean userHasEditedPassword, isFirstTimeUse;
    private String pref_user_phone_number, pref_user_password, pref_user_token, pref_user_ipAddress;
    private String captchaCode;
    private SharedPreferences sharedPreferences;
    private OkHttpClient client;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS) // 写入超时时间
                .build();
        context = this;
        // 初始化组件
        initialComponent();
        // 初始化用户数据
        initializeData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("LoginActivity", "onRestart");
        // 初始化用户数据
        initializeData();
    }

    // 初始化组件
    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent() {
        setContentView(R.layout.activity_login);
        phone_number = findViewById(R.id.phone_number); // 获取手机号输入框
        password = findViewById(R.id.password); // 获取密码输入框
        forgot_password = findViewById(R.id.forgot_password); // 获取忘记密码按钮
        register = findViewById(R.id.register); // 获取注册按钮
        login_button = findViewById(R.id.login_button);

        btn_debug = findViewById(R.id.btn_debug);

        // 设置手机号输入框的输入限制
        phone_number.addTextChangedListener(new CustomTextWatcher(phone_number,
                InputDataVerificationUtils::phoneNumberInputVerification, "请输入正确的手机号", 11, () -> password.requestFocus()));

        // 设置密码输入框的输入限制
        password.addTextChangedListener(new CustomTextWatcher(password,
                InputDataVerificationUtils::passwordInputVerification, "密码至少包含8个字符，可包含数字、大小写字母和符号", false));

        // 设置密码输入框的监听事件, 当用户编辑自动输入的密码时，清空本地存储的密码
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 检查用户是否编辑过密码
                if (!userHasEditedPassword) {
                    // 清空密码
                    password.getText().clear();
                    userHasEditedPassword = true;
                    clearPassword();
                }
            }
        });

        // 设置密码输入框的显示状态
        password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - password.getPaddingEnd())) {
                    // 切换密码的显示状态
                    if (password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                        // 当密码为自动填入时，用户尝试查看密码，清空密码。
                        if (!userHasEditedPassword) {
                            password.getText().clear();
                            userHasEditedPassword = true;
                            clearPassword();
                        }
                    } else {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    // 将光标移动到文本末尾
                    password.setSelection(password.getText().length());
                    v.performClick(); // 添加这行代码
                    return true;
                }
            }
            return false;
        });

        // 设置忘记密码按钮的点击事件
        forgot_password.setOnClickListener(this);

        // 设置注册按钮的点击事件
        register.setOnClickListener(this);
        // 点击登录按钮后
        login_button.setOnClickListener(this);
        btn_debug.setOnClickListener(this);
    }

    // 初始化用户数据
    private void initializeData() {
        // 获取用户IP地址
        pref_user_ipAddress = getIpAddress(this);

        userHasEditedPassword = true;

        sharedPreferences = this.getSharedPreferences("default", MODE_PRIVATE);
        pref_user_phone_number = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        pref_user_password = sharedPreferences.getString(getString(R.string.pref_user_password), "");
        pref_user_token = sharedPreferences.getString(getString(R.string.pref_user_token), "");
        isFirstTimeUse = sharedPreferences.getBoolean(getString(R.string.pref_first_run), true);

        Log.d(TAG, "pref_user_phone_number: " + pref_user_phone_number);
        Log.d(TAG, "pref_user_password: " + pref_user_password);
        Log.d(TAG, "pref_user_token: " + pref_user_password);

        // 如果用户没有保存手机号或密码，则直接返回
        if (pref_user_phone_number.isEmpty() || pref_user_password.isEmpty()) {
            return;
        }

        phone_number.setText(pref_user_phone_number);
        password.setText(pref_user_password);

        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);

        // 用于判断用户有没有编辑密码框
        userHasEditedPassword = false;
    }

    // 检查输入框格式信息。
    private boolean checkLogin() {
        String phoneNumber = phone_number.getText().toString();
        String password = this.password.getText().toString();
        if (!InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber)) {
            phone_number.setError("请输入正确的手机号");
            Toast.makeText(LoginActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.passwordInputVerification(password)) {
            this.password.setError("密码至少包含8个字符，可包含数字、大小写字母和符号", null);
            Toast.makeText(LoginActivity.this, "密码至少包含8个字符，可包含数字、大小写字母和符号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber) && InputDataVerificationUtils.passwordInputVerification(password)) {
            // 登录
            return true;
        }
        return false;
    }


    // 当用户编辑密码框时，清空本地存储的密码
    private void clearPassword() {
        Log.v("LoginActivity", "clearPassword 被调用");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_user_password), "");
        editor.apply();
    }

    // 服务器验证登录信息
    private void serverVerification(ServerLoginVerificationCallback verificationCallback) throws JSONException {
        String uri = serverUrl + "/users/login";
        MediaType JSON = MediaType.get("application/json");
        JSONObject userObject = new JSONObject();
        userObject.put("username", phone_number.getText().toString());
        userObject.put("password", password.getText().toString());
        // todo:添加数字验证码输入
        userObject.put("captcha", captchaCode);
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("user", userObject);

        String json = jsonObject.toString();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                verificationCallback.onFailed(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    // 登录成功！
                    // 解析响应结果
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        pref_user_phone_number = userObject.getString("username");
                        pref_user_token = userObject.getString("token");
                        verificationCallback.onSuccess();
                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
                        Log.e(TAG, "json解析失败！" + e);
                        // 添加错误处理代码
                        verificationCallback.onFailed("服务器返回的数据格式错误，请稍后再试");
                    }

                } else {
                    String message = "";
                    if (response.body() != null) {
                        String string = response.body().string();
                        if (!string.isEmpty()) {
                            try {
                                JSONObject jsonObject1 = new JSONObject(string);
                                JSONArray jsonArray = jsonObject1.getJSONArray("errors");
                                StringBuilder sb = new StringBuilder(message);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    sb.append(jsonArray.getString(i));
                                }
                                message = sb.toString();
                            } catch (JSONException e) {
//                            throw new RuntimeException(e);
                                Log.e(TAG, "json解析失败！" + e);
                                // 添加错误处理代码
                                runOnUiThread(() -> {
                                    Toast.makeText(context, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }
                    verificationCallback.onFailed(message);
                }

            }
        });
    }

    private void setComponentsEnabled(boolean enabled) {
        phone_number.setEnabled(enabled);
        password.setEnabled(enabled);
        forgot_password.setEnabled(enabled);
        login_button.setEnabled(enabled);
        register.setEnabled(enabled);
    }

    private void disableAllComponents() {
        setComponentsEnabled(false);
    }

    private void enableAllComponents() {
        setComponentsEnabled(true);
    }

    // 处理点击事件
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_button) {
            if (checkLogin()) {  // 检查输入框格式信息。
                disableAllComponents();  // 禁用所有组件
                VerificationCodeDialogFragment verificationCodeDialogFragment = VerificationCodeDialogFragment.newInstance(
                        phone_number.getText().toString(), new VerificationCodeDialogFragment.GetCaptchaCodeCallback() {
                            @Override
                            public void getCode(String code) {
                                captchaCode = code;
                                Log.d(TAG, "用户已输入数字验证码：" + code);
                                try {
                                    // 在服务器校验登录信息
                                    serverVerification(new ServerLoginVerificationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            // 登录成功，保存账号密码token，跳转到主页面
                                            SharedPreferences sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor edit = sharedPreferences.edit();
                                            edit.putString(getString(R.string.pref_user_phone_number), phone_number.getText().toString());
                                            edit.putString(getString(R.string.pref_user_password), password.getText().toString());
                                            edit.putString(getString(R.string.pref_user_token), pref_user_token);
                                            edit.putString(getString(R.string.pref_user_ipAddress), pref_user_ipAddress);
                                            edit.apply();

                                            Intent i;
                                            if (isFirstTimeUse) {
                                                i = new Intent(LoginActivity.this, IntroGuideActivity.class);
                                            } else {
                                                i = new Intent(LoginActivity.this, DeviceListActivity.class);
                                            }
                                            startActivity(i);
                                            finish();
                                        }

                                        @Override
                                        public void onFailed(String msg) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, "登录失败！\n" + msg, Toast.LENGTH_LONG).show();
                                                    enableAllComponents();
                                                }
                                            });
                                        }
                                    });
                                } catch (JSONException e) {
                                    runOnUiThread(() -> enableAllComponents());
                                    Toast.makeText(context, "本地信息异常，请重启软件后尝试！", Toast.LENGTH_LONG).show();
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onDialogDismiss(boolean isDismissedWithResult) {
                                if (!isDismissedWithResult) {
                                    enableAllComponents();
                                }
                            }
                        }
                );
                verificationCodeDialogFragment.show(getSupportFragmentManager(), "verificationCodeDialogFragment");

            }
        } else if (view.getId() == R.id.register) {
            // 跳转到注册页面
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        } else if (view.getId() == R.id.forgot_password) {
            Toast.makeText(LoginActivity.this, "功能开发中...", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.btn_debug) {
            Intent i;
            if (isFirstTimeUse) {
                i = new Intent(LoginActivity.this, IntroGuideActivity.class);
            } else {
                i = new Intent(LoginActivity.this, DeviceListActivity.class);
            }
            startActivity(i);
            finish();
        }
    }

    // 用于获取IP地址
    @SuppressLint("DefaultLocale")
    public String getIpAddress(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // Wi-Fi 连接
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                return String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // 移动数据连接
                try {
                    List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                    for (NetworkInterface intf : interfaces) {
                        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                        for (InetAddress addr : addrs) {
                            if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                                return addr.getHostAddress();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public interface ServerLoginVerificationCallback {
        void onSuccess();

        void onFailed(String msg);
    }
}
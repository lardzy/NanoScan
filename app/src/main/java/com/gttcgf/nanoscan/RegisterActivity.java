package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerificationUtils;
import com.gttcgf.nanoscan.tools.PortraitCaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String serverUrl = "https://newnirtechnolgy.top/api";
    private static final String TAG = "RegisterActivity";
    private EditText phone_number, sms_verification_code, password, check_code;
    private ProgressBar progress;
    private Button register_button;
    private ImageButton imageButton_back;
    private TextView get_verification_code, register;
    private Handler handler;
    private String digit_code, token, username;
    private int fetchCaptchaSecondsRemaining;
    // 用于获取短信验证码按钮的倒计时
    private final Runnable updateCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (fetchCaptchaSecondsRemaining > 0) {
                get_verification_code.setClickable(false);
                get_verification_code.setText(String.valueOf(fetchCaptchaSecondsRemaining) + "s");
                fetchCaptchaSecondsRemaining--;
                handler.postDelayed(this, 1000);
            } else {
                get_verification_code.setClickable(true);
                get_verification_code.setText(getResources().getString(R.string.get_verification_code));
            }
        }
    };
    private Context context;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        this.context = this;
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS) // 写入超时时间
                .build();

        handler = new Handler(Looper.getMainLooper());
        // 初始化布局组件
        initialComponent();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 将扫描授权码赋值到文本框。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "扫描取消", Toast.LENGTH_LONG).show();
            } else {
                check_code.setText(result.getContents());
                check_code.setSelection(check_code.getText().length());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 初始化布局组件
    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent() {
        phone_number = findViewById(R.id.phone_number);
        sms_verification_code = findViewById(R.id.sms_verification_code);
        password = findViewById(R.id.password);
        check_code = findViewById(R.id.check_code);
        register_button = findViewById(R.id.register_button);
        imageButton_back = findViewById(R.id.imageButton_back);
        get_verification_code = findViewById(R.id.get_verification_code);
        register = findViewById(R.id.register);
        progress = findViewById(R.id.progress);

        // 设置手机号输入框的输入限制
        phone_number.addTextChangedListener(new CustomTextWatcher(phone_number,
                InputDataVerificationUtils::phoneNumberInputVerification, "请输入正确的手机号", 11, () -> sms_verification_code.requestFocus()));
        // 设置短信验证码输入框的输入限制
        sms_verification_code.addTextChangedListener(new CustomTextWatcher(sms_verification_code,
                InputDataVerificationUtils::smsVerificationCodeVerification, "请输入正确的验证码", false));
        // 设置密码输入框的输入限制
        password.addTextChangedListener(new CustomTextWatcher(password,
                InputDataVerificationUtils::passwordInputVerification, "密码至少包含8个字符，可包含数字、大小写字母和符号", false));
        // 设置设备授权码输入框的输入限制
        check_code.addTextChangedListener(new CustomTextWatcher(check_code,
                InputDataVerificationUtils::checkCodeVerification, "请输入正确的授权码，授权码位于机身正面的机身按钮下侧"));
        // 扫描授权码
        check_code.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (check_code.getRight() - check_code.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - check_code.getCompoundDrawablePadding())) {
                    // 扫描条形码
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setOrientationLocked(true); // 锁定屏幕方向
                    integrator.setCaptureActivity(PortraitCaptureActivity.class); // 使用自定义活动
                    integrator.initiateScan();
                    return true;
                }
            }
            return false;
        });
        imageButton_back.setOnClickListener(v -> finish());

        // 已有账号登录按钮的点击事件，如果登录界面已经打开，则返回，否则重新打开登录界面。
        register.setOnClickListener(v -> {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (ActivityManager.RunningTaskInfo task : tasks) {
                if ("com.gttcgf.nanoscan.LoginActivity".equals(task.baseActivity.getClassName())) {
                    finish();
                    return;
                }
            }
            // 如果登录界面没有打开，则重新打开登录界面。
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // 获取验证码按钮的点击事件
        get_verification_code.setOnClickListener(v -> {
            if (!InputDataVerificationUtils.phoneNumberInputVerification(phone_number.getText().toString())) {
                phone_number.setError("请输入正确的手机号");
                Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            DialogFragment df = VerificationCodeDialogFragment.newInstance(phone_number.getText().toString(), new VerificationCodeDialogFragment.GetCaptchaCodeCallback() {
                @Override
                public void getCode(String code) {
                    // 如果获取的验证码不为空，则更新验证码文本框，并且设定冷却时间
                    if (!code.isEmpty()) {
                        digit_code = code;
                        sms_verification_code.setText(code);
                        startCountingDown(5);
                    }
                }

                @Override
                public void onDialogDismiss(boolean isDismissedWithResult) {

                }

            });
            // 显示弹窗
            df.show(getSupportFragmentManager(), "VerificationCodeDialogFragment");

        });

        // 点击立即注册按钮的事件
        register_button.setOnClickListener(v -> {
            if (checkComponents()) {
                disableAllComponents();
                try {
                    serverVerification(new ServerRegisterVerificationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                // 注册成功后，保存用户名、密码、token
                                SharedPreferences sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
                                SharedPreferences.Editor edit = sharedPreferences.edit();
                                edit.putString(getString(R.string.pref_user_phone_number), phone_number.getText().toString());
//                                String passwordHash = PasswordUtils.hashPassword(password.getText().toString());
                                edit.putString(getString(R.string.pref_user_password), password.getText().toString());
                                edit.putString(getString(R.string.pref_user_token), token).apply();
                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }

                        @Override
                        public void onFailed(String msg) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "注册失败！\n" + msg, Toast.LENGTH_LONG).show();
                                // 如果注册失败则清空图形验证码框
                                sms_verification_code.setText("");
                                enableAllComponents();
                            });

                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(this::enableAllComponents);
                    Toast.makeText(context, "本地信息异常，请重启软件后尝试！", Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
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
    }

    private void startCountingDown(int i) {
        if (i <= 0) {
            return;
        }
        fetchCaptchaSecondsRemaining = i;
        handler.post(updateCountdownRunnable);
    }

    // 检查输入内容是否合法，如果合法则继续注册，否则提示错误。
    private boolean checkComponents() {
        String phoneNumber = phone_number.getText().toString();
        String smsCode = sms_verification_code.getText().toString();
        String password = this.password.getText().toString();
        String checkCode = check_code.getText().toString();
        if (!InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber)) {
            phone_number.setError("请输入正确的手机号");
            Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.smsVerificationCodeVerification(smsCode)) {
            sms_verification_code.setError("请输入正确的验证码", null);
            Toast.makeText(RegisterActivity.this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.passwordInputVerification(password)) {
            this.password.setError("密码至少包含8个字符，可包含数字、大小写字母和符号", null);
            Toast.makeText(RegisterActivity.this, "密码至少包含8个字符，可包含数字、大小写字母和符号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.checkCodeVerification(checkCode)) {
            check_code.setError("请输入正确的授权码，授权码位于机身正面的机身按钮下侧");
            Toast.makeText(RegisterActivity.this, "请输入正确的授权码，授权码位于机身正面的机身按钮下侧", Toast.LENGTH_SHORT).show();
            return false;
        } else if (InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber)
                && InputDataVerificationUtils.smsVerificationCodeVerification(smsCode) &&
                InputDataVerificationUtils.passwordInputVerification(password) &&
                InputDataVerificationUtils.checkCodeVerification(checkCode)) {
            // 注册
            return true;
        }
        return false;
    }

    // 将注册信息发送至服务器进行验证。
    private void serverVerification(ServerRegisterVerificationCallback serverRegisterVerificationCallback) throws JSONException {
        String uri = serverUrl + "/users/register";
        MediaType JSON = MediaType.get("application/json");
        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();
        jsonObject.put("username", phone_number.getText().toString());
        jsonObject.put("password", password.getText().toString());
        jsonObject.put("captcha", digit_code);
        jsonObject.put("deviceauthorizationcode", check_code.getText().toString());
        userObject.put("user", jsonObject);
        String json = userObject.toString();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                serverRegisterVerificationCallback.onFailed(e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 201 && response.body() != null) {
                    // 如果注册成功
                    // 解析响应结果
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        username = userObject.getString("username");
                        token = userObject.getString("token");
                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
                        Log.e(TAG, "json解析失败！" + e);
                        // 添加错误处理代码
                        runOnUiThread(() -> {
                            Toast.makeText(context, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                        });
                    }
                    serverRegisterVerificationCallback.onSuccess();
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
                                Log.e(TAG, "json解析失败！" + e);
                                // 添加错误处理代码
                                runOnUiThread(() -> {
                                    Toast.makeText(context, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }
                    serverRegisterVerificationCallback.onFailed(message);
                }
            }
        });
    }


    private void disableAllComponents() {
        setComponentsEnabled(false);
    }

    private void enableAllComponents() {
        setComponentsEnabled(true);
    }

    private void setComponentsEnabled(boolean enabled) {
        phone_number.setEnabled(enabled);
        sms_verification_code.setEnabled(enabled);
        password.setEnabled(enabled);
        check_code.setEnabled(enabled);
        progress.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    public interface ServerRegisterVerificationCallback {
        void onSuccess();

        void onFailed(String meg);
    }
}
package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerificationUtils;
import com.gttcgf.nanoscan.tools.PortraitCaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DevicePermissionCheckFragment extends DialogFragment implements View.OnClickListener {  // 选择蓝牙设备时，弹出的验证弹窗
    private static final String TAG = "DevicePermissionCheckFr";
    private Context context;
    private Handler handler;
    private EditText check_code;
    private TextView tv_dialog_subtitle;
    private Button buttonCancel, buttonConfirm;
    private ProgressBar button_confirm_progress;
    private static final String serverUrl = "https://newnirtechnolgy.top/api";
    private OkHttpClient client;
    private String username, password, pcode, mcode, token, deviceToken;
    private VerifyDevicePermissionCallback permissionCallback;

    public static DevicePermissionCheckFragment newInstance(Bundle bundle, VerifyDevicePermissionCallback permissionCallback) {
        DevicePermissionCheckFragment fragment = new DevicePermissionCheckFragment();
        fragment.setPermissionCallback(permissionCallback);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 获取存储的Bundle
        this.context = getActivity();
        handler = new Handler();
        View view = getLayoutInflater().inflate(R.layout.fragment_select_device_view_dialog, container, false);

        Log.d(TAG, "添加设备授权弹窗-弹窗视图已创建！");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(context, "扫描取消", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "添加设备授权弹窗-得到条形码扫描结果");
                check_code.setText(result.getContents());
                check_code.setSelection(check_code.getText().length());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "添加设备授权弹窗-视图已创建完成");

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonConfirm = view.findViewById(R.id.button_confirm);
        check_code = view.findViewById(R.id.check_code);
        button_confirm_progress = view.findViewById(R.id.button_confirm_progress);
        tv_dialog_subtitle = view.findViewById(R.id.tv_dialog_subtitle);

        tv_dialog_subtitle.setText(context.getResources().getString(R.string.nano_confirmation_msg, mcode));
        check_code.addTextChangedListener(new CustomTextWatcher(check_code, InputDataVerificationUtils::checkCodeVerification,
                "请输入正确的授权码"));
        check_code.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (check_code.getRight() - check_code.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - check_code.getCompoundDrawablePadding())) {
                        // 扫描条形码
                        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(DevicePermissionCheckFragment.this);
                        integrator.setOrientationLocked(true); // 锁定屏幕方向
                        integrator.setCaptureActivity(PortraitCaptureActivity.class); // 使用自定义活动
                        integrator.initiateScan();
                        return true;
                    }
                }
                return false;
            }
        });

        // 点击弹窗的确认按钮
        buttonConfirm.setOnClickListener(this);
        // 点击弹窗的取消按钮
        buttonCancel.setOnClickListener(this);
    }

    // 用于校验“校验码”的内容格式和服务器授权
    private void verifyCheckCode(String result, VerifyDevicePermission devicePermission) throws JSONException {
        if (result.isEmpty()) {
            Log.e(TAG, "添加设备授权弹窗-授权码输入为空");
            devicePermission.onFailed();
            return;
        }
        String reg = "[a-zA-Z0-9]{3,16}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            Log.e(TAG, "添加设备授权弹窗-授权码输入框内容长度或格式非法");
            devicePermission.onFailed();
            return;
        }

        // 授权码格式正确，继续进行服务器授权验证

        String url = serverUrl + "/add_machine";
        MediaType mediaType = MediaType.get("application/json");
        JSONObject userObject = new JSONObject();
        userObject.put("username", username);
        userObject.put("password", password);
        userObject.put("deviceauthorizationcode", result);
        userObject.put("pcode", pcode);
        userObject.put("mcode", mcode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", userObject);

        String json = jsonObject.toString();

        Log.d(TAG, "添加设备授权弹窗-请求体内容已经构建完成：\n" + json);

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "添加设备授权弹窗-服务器请求失败，考虑网络问题");
                devicePermission.onFailed();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    Log.e(TAG, "添加设备授权弹窗-服务器请求成功！\ncode:" + response.code());
                    // 登录成功！
                    // 解析响应结果
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        username = userObject.getString("username");
                        deviceToken = userObject.getString("token");

                        Log.d(TAG, "添加设备授权弹窗-服务器响应体解析成功！\nusername:" + username + "\ndeviceToken:" + deviceToken);
                        devicePermission.onSuccess();
                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
                        Log.e(TAG, "添加设备授权弹窗-服务器响应体json解析失败！\n" + e);
                        // 添加错误处理代码
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(context, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                            });
                        }
                        devicePermission.onFailed();
                    }
                } else {
                    Log.e(TAG, "添加设备授权弹窗-服务器响应代码为错误！\ncode:" + response.code());
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
                                Log.e(TAG, "添加设备授权弹窗-服务器响应体json解析失败！\n" + e);
                                // 添加错误处理代码
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(context, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        }
                    }
                    devicePermission.onFailed();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_confirm) {
            Log.d(TAG, "添加设备授权弹窗-确认按钮已按下");

            EnableAllComponent(false);
            buttonConfirm.setText("");
            button_confirm_progress.setVisibility(View.VISIBLE);

            try {
                verifyCheckCode(check_code.getText().toString(), new VerifyDevicePermission() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "添加设备授权弹窗-设备授权码验证成功！");
                        permissionCallback.onSuccess(deviceToken);
                        if (isResumed()) {
                            dismiss();
                        }
                    }

                    @Override
                    public void onFailed() {
                        Log.e(TAG, "添加设备授权弹窗-设备授权码验证失败！");
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buttonConfirm.setText(getString(R.string.confirm));
                                    button_confirm_progress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(activity, "设备授权失败！请检查授权码是否正确。", Toast.LENGTH_LONG).show();
                                    EnableAllComponent(true);
                                }
                            });
                        }
                    }
                });
            } catch (JSONException e) {
                // todo:进行异常处理
                throw new RuntimeException(e);
            }

        } else if (view.getId() == R.id.button_cancel) {
            dismiss();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Dialog dismissed");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // 设置无标题样式
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置参数，获取到窗口的尺寸
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                // 获取窗口对象和参数
                WindowManager.LayoutParams params = window.getAttributes();
                // 设置宽度和高度为屏幕的70%
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);
                // 将设置好的参数应用到窗口
                window.setAttributes(params);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS) // 写入超时时间
                .build();
        // 初始化接收到的数据（用户名等）
        initialData();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initialData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            username = bundle.getString("username", "");
            password = bundle.getString("password", "");
            pcode = bundle.getString("pcode", "");
            mcode = bundle.getString("mcode", "");
            token = bundle.getString("token", "");
        }

    }

    private void EnableAllComponent(boolean enable) {
        check_code.setEnabled(enable);
        buttonConfirm.setEnabled(enable);
        buttonCancel.setEnabled(enable);
    }

    public interface VerifyDevicePermission {
        void onSuccess();

        void onFailed();
    }


    public interface VerifyDevicePermissionCallback {
        void onSuccess(String token);

        void onFailed();
    }

    public void setPermissionCallback(VerifyDevicePermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }
}

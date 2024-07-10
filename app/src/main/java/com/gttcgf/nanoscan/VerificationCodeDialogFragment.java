package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VerificationCodeDialogFragment extends DialogFragment {
    private static final long fetchCaptchaImage_DELAY = 1000;  // 刷新验证码的间隔时长
    private static final String TAG = "VerificationCodeDialogF";
    private static final String serverUrl = "https://newnirtechnolgy.top/api";
    private OkHttpClient client;
    private Context context;
    private String phone_number;
    private ImageView imageViewVerificationCode;
    private ProgressBar progressBar;
    private EditText editTextVerificationInput;
    private Handler handler;
    private GetCaptchaCodeCallback getCaptchaCodeCallback;
    private boolean isDismissedWithResult = false;
    private boolean fetchCaptchaImage_clickable = true;  // 验证码图片是否可以更新

    public static VerificationCodeDialogFragment newInstance(String phone_number, GetCaptchaCodeCallback getCaptchaCodeCallback) {
        VerificationCodeDialogFragment fragment = new VerificationCodeDialogFragment();
        fragment.setGetCaptchaCodeCallback(getCaptchaCodeCallback);
        Bundle args = new Bundle();
        args.putString("PHONE_NUMBER", phone_number);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 获取存储的Bundle
        this.context = getActivity();
        this.phone_number = getArguments().getString("PHONE_NUMBER");
//        View view = getLayoutInflater().inflate(R.layout.dialog_verification_code_image, container, false);
        View view = inflater.inflate(R.layout.dialog_verification_code_image, container, false);

        handler = new Handler();

        fetchCaptchaImage(phone_number);
        return view;
    }

    // 获取验证码图片
    private void fetchCaptchaImage(String phone_number) {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS) // 写入超时时间
                .build();
//        HttpUrl.Builder urlBuilder = HttpUrl.parse(serverUrl + "/request_digit_code").newBuilder();
//        urlBuilder.addQueryParameter("phone_number", phone_number);
//        String url = urlBuilder.toString();
        String url = serverUrl + "/captcha/" + phone_number;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "验证码请求失败！\n" + e, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
                }

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    byte[] imageBytes = null;
                    if (response.body().contentType().toString().equals("application/json")) {
                        String responseData = response.body().string();
                        String captchaImage;
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            // 只取逗号后的内容
                            captchaImage = jsonObject.getString("captcha_img").split(",")[1];
                        } catch (JSONException e) {
                            Log.e(TAG, "json解码失败！\n" + e);
                            throw new RuntimeException(e);
                        }
                        // 解码图片
                        imageBytes = Base64.decode(captchaImage, Base64.DEFAULT);

                    } else if (response.body().contentType().toString().equals("image/png")) {
                        imageBytes = response.body().bytes();
                    }
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    FragmentActivity activity = getActivity();
                    if (activity != null && bitmap != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 验证码图片获取成功
                                progressBar.setVisibility(View.INVISIBLE);
                                imageViewVerificationCode.setVisibility(View.VISIBLE);
                                imageViewVerificationCode.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        Log.e(TAG, "FragmentActivity为null!");
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "验证码请求失败！" + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    // 对输入的验证码进行格式校验
    private void verifyCaptchaCode(String result, VerifyCaptchaCallback callback) {
        if (result.isEmpty()) {
            callback.onFailed();
            return;
        }
        String reg = "[a-zA-Z0-9]{6}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            callback.onFailed();
            return;
        }
        // 验证码格式正确
        callback.onSuccess();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // 设置无标题样式
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置参数，获取到窗口的尺寸
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 获取窗口对象和参数
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                WindowManager.LayoutParams params = window.getAttributes();
                // 设置宽度为屏幕的70%
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
//                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);
                // 将设置好的参数应用到窗口
                window.setAttributes(params);
            }

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonConfirm = view.findViewById(R.id.button_confirm);
        Button buttonCancel = view.findViewById(R.id.button_cancel);
        imageViewVerificationCode = view.findViewById(R.id.imageViewVerificationCode);
        progressBar = view.findViewById(R.id.progressBar);
        editTextVerificationInput = view.findViewById(R.id.editTextVerificationInput);

        progressBar.setVisibility(View.VISIBLE);

        // 点击了验证码窗口的确认按钮
        buttonConfirm.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            buttonConfirm.setClickable(false);
            verifyCaptchaCode(editTextVerificationInput.getText().toString(), new VerifyCaptchaCallback() {
                @Override
                public void onSuccess() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isDismissedWithResult = true;
                            // 调用注册界面RegisterActivity的接口实现类，传递验证码数字
                            getCaptchaCodeCallback.getCode(editTextVerificationInput.getText().toString());
                            dismiss();
                        }
                    });

                }

                @Override
                public void onFailed() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonConfirm.setClickable(true);
                            editTextVerificationInput.setText("");
                            editTextVerificationInput.setError("验证码格式错误，请重试！");
                            // 重新获取验证码图片
                            progressBar.setVisibility(View.INVISIBLE);
//                            fetchCaptchaImage(phone_number);
                        }
                    });

                }
            });
        });

        // 点击了验证码窗口的取消按钮
        buttonCancel.setOnClickListener(v -> {
            dismiss();
        });
        // 设置验证码图案的点击事件监听，点击时进行刷新，频率限制为1秒一次。
        imageViewVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fetchCaptchaImage_clickable) {
                    progressBar.setVisibility(View.VISIBLE);
                    fetchCaptchaImage(phone_number);
                    fetchCaptchaImage_clickable = false;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchCaptchaImage_clickable = true;
                        }
                    }, fetchCaptchaImage_DELAY);
                }

            }
        });
    }

    public void setGetCaptchaCodeCallback(GetCaptchaCodeCallback getCaptchaCodeCallback) {
        this.getCaptchaCodeCallback = getCaptchaCodeCallback;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Dialog dismissed");
        if (getCaptchaCodeCallback != null) {
            getCaptchaCodeCallback.onDialogDismiss(isDismissedWithResult);
        }
    }

    public interface VerifyCaptchaCallback {
        void onSuccess();

        void onFailed();
    }

    public interface GetCaptchaCodeCallback {
        void getCode(String code);

        void onDialogDismiss(boolean isDismissedWithResult);
    }
}

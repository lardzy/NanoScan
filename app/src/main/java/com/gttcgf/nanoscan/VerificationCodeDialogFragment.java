package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.Context;
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
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerificationCodeDialogFragment extends DialogFragment {
    private static final long fetchCaptchaImage_DELAY = 1000;  // 刷新验证码的间隔时长
    private static final String TAG = "VerificationCodeDialogF";
        private static final String serverUrl = "https://newnirtechnolgy.top/api";
//    private static final String serverUrl = "https://newnirtechnolgy.top";
    private OkHttpClient client;
    private Context context;
    private String phone_number;
    private ImageView imageViewVerificationCode;
    private ProgressBar progressBar;
    private EditText editTextVerificationInput;
    private Handler handler;
    private GetCaptchaCodeCallback getCaptchaCodeCallback;
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
        View view = inflater.inflate(R.layout.dialog_verification_code_image, container, false);

        handler = new Handler();

        imageViewVerificationCode = view.findViewById(R.id.imageViewVerificationCode);
        progressBar = view.findViewById(R.id.progressBar);
        editTextVerificationInput = view.findViewById(R.id.editTextVerificationInput);

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
            public void onResponse(Call call, Response response) throws IOException {
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
                                progressBar.setVisibility(View.GONE);
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
//                                dismiss();
                            }
                        });
                    }
                }
            }
        });
    }

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

        client = new OkHttpClient();
        String url = serverUrl + "/verify_digit_code";
        MediaType JSON = MediaType.get("application/json");
        String json = "{\"phone_number\":\"" + phone_number + "\", \"digit_code\":\"" + result + "\"}";
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(context, "请求失败！\n" + e, Toast.LENGTH_SHORT).show());
                    callback.onFailed();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "图形验证码校验成功！" + result, Toast.LENGTH_LONG).show();
                            callback.onSuccess();
                        }
                    });
                } else {
                    callback.onFailed();
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // 设置参数，获取到窗口的尺寸
        dialog.setOnShowListener(dialogInterface -> {
            // 获取窗口对象和参数
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();

            // 设置宽度和高度为屏幕的70%
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
//            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);

            // 将设置好的参数应用到窗口
            window.setAttributes(params);
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        // 验证码弹窗点击确认后
        buttonConfirm.setOnClickListener(v -> {
            progressBar.setVisibility(View.GONE);
            buttonConfirm.setClickable(false);
            // 内容为空时，直接标红
//            if (editTextVerificationInput.getText().toString().isEmpty()) {
//                editTextVerificationInput.setError("验证码为空！");
//            }
            verifyCaptchaCode(editTextVerificationInput.getText().toString(), new VerifyCaptchaCallback() {
                @Override
                public void onSuccess() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                            editTextVerificationInput.setError("验证码错误，请重试！");
                            fetchCaptchaImage(phone_number);
                        }
                    });

                }
            });
        });

        buttonCancel.setOnClickListener(v -> {
            dismiss();
        });
        imageViewVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fetchCaptchaImage_clickable) {
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

    public interface VerifyCaptchaCallback {
        void onSuccess();

        void onFailed();
    }

    public interface GetCaptchaCodeCallback {
        void getCode(String code);
    }
}

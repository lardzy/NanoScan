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
    private OkHttpClient client;
    private Context context;
    private String phone_number;
    private ImageView imageViewVerificationCode;
    private ProgressBar progressBar;
    private EditText editTextVerificationInput;
    private Handler handler;
    private static final long fetchCaptchaImage_DELAY = 1000;  // 刷新验证码的间隔时长
    private boolean fetchCaptchaImage_clickable = true;  // 验证码图片是否可以更新
    private static final String TAG = "VerificationCodeDialogF";
    private static final String serverUrl = "http://8.138.102.24:5000";

    public static VerificationCodeDialogFragment newInstance(String phone_number) {
        VerificationCodeDialogFragment fragment = new VerificationCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("PHONE_NUMBER", phone_number);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    private void fetchCaptchaImage(String phone_number) {
        client = new OkHttpClient();
//        String url = serverUrl + "/request_digit_code?";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(serverUrl + "/request_digit_code").newBuilder();
        urlBuilder.addQueryParameter("phone_number", phone_number);
        String url = urlBuilder.toString();

        Request request = new Request.Builder()
                .url(url + "phone_number=" + phone_number)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(context, "验证码请求失败！\n" + e, Toast.LENGTH_SHORT).show());
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
                        getActivity().runOnUiThread(() -> Toast.makeText(context, "验证码请求失败！", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private boolean verifyCaptchaCode(String result) {
        if (result.isEmpty()) {
            return false;
        }
        String reg = "[a-zA-Z0-9]{6}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            return false;
        }

        client = new OkHttpClient();
        String url = serverUrl + "/verify_digit_code";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
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
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "请求成功！" + result , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return false;
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
        buttonConfirm.setOnClickListener(v -> {
            progressBar.setVisibility(View.GONE);
            buttonConfirm.setClickable(false);
            Toast.makeText(getActivity(), "确认按钮被点击", Toast.LENGTH_SHORT).show();
            boolean result = verifyCaptchaCode(editTextVerificationInput.getText().toString());
            if (result) {
                dismiss();
            } else {
                buttonConfirm.setClickable(true);
                editTextVerificationInput.setError("验证码错误，请重试！");
                fetchCaptchaImage(phone_number);
            }

//            dismiss();
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
}

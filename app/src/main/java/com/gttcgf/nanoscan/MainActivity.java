package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String serverUrl = "https://newnirtechnolgy.top/api";
    private ImageButton ib_shutdown, ib_account, ib_add_device;
    private EditText et_search;
    private RecyclerView rv_devices_list;
    private ProgressBar pb_devices_list, pb_news;
    private TextView tv_devices_list_empty, tv_nes_empty;
    private List<DeviceItem> deviceItem, newDeviceItemList;
    private MainActivityDeviceListAdapter deviceListAdapter;
    private OkHttpClient client;
    private Context mContext;
    private String username, loginToken, userPhoneNumber;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_NanoScan);
        super.onCreate(savedInstanceState);
        Log.e(TAG, "主界面-onCreate called");
        // Set the status bar to transparent
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(30, TimeUnit.SECONDS) // 读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS) // 写入超时时间
                .build();

        mContext = this;

        initializeData();
        initComponent();
        // 检查用户是否是第一次使用、检查用户是否已登录
        checkIsFirstTimeUse();
        // 应用窗口边缘到边缘的设置
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "主界面-onResume called!");
        // todo:这里验证登录

        checkUserLoginStatus();
        // 更新列表数据和UI
        updateData();
    }


    private void updateData() {
        Log.d(TAG, "主界面-正在尝试更新数据到newDeviceItemList...");
        newDeviceItemList = SpectralDataUtils.readDeviceListFromFile(this, userPhoneNumber);
        if (newDeviceItemList != null) {
            Log.d(TAG, "主界面-新数据不为null，正在更新到设备列表Adapter；newDeviceItemList size：" + newDeviceItemList.size());
            deviceListAdapter.updateDeviceList(newDeviceItemList);
            this.deviceItem = newDeviceItemList;
        } else {
            Log.e(TAG, "主界面-newDeviceItemList为null?检查本地文件完整性！");
        }
        updateEmptyState();
    }

    private void initializeData() {
        Log.e(TAG, "主界面-initializeData called");
        // 读取本地文件
        sharedPreferences = this.getSharedPreferences("default", Context.MODE_PRIVATE);
        userPhoneNumber = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        this.deviceItem = SpectralDataUtils.readDeviceListFromFile(this, userPhoneNumber);
        Log.d(TAG, "主界面-设备列表文件读取长度为：" + deviceItem.size());
        updateEmptyState();
        // todo:增加获取消息列表的功能
    }

    private void initComponent() {
        Log.e(TAG, "主界面-initComponent called!");

        ib_shutdown = findViewById(R.id.ib_shutdown);
        ib_account = findViewById(R.id.ib_account);
        ib_add_device = findViewById(R.id.ib_add_device);
        et_search = findViewById(R.id.et_search);
        rv_devices_list = findViewById(R.id.rv_devices_list);
        pb_devices_list = findViewById(R.id.pb_devices_list);
        pb_news = findViewById(R.id.pb_news);
        tv_devices_list_empty = findViewById(R.id.tv_devices_list_empty);
        tv_nes_empty = findViewById(R.id.tv_nes_empty);

        et_search.setEnabled(false);
        ib_shutdown.setOnClickListener(this);
        ib_add_device.setOnClickListener(this);
        ib_account.setOnClickListener(this);

        pb_news.setVisibility(View.INVISIBLE);
        tv_nes_empty.setVisibility(View.VISIBLE);
        rv_devices_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // 设置RecycleView的适配器
        deviceListAdapter = new MainActivityDeviceListAdapter(deviceItem);
        rv_devices_list.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener(position -> {
            // 点击设备列表中的设备，跳转到设备详情页面
            Intent i = new Intent(MainActivity.this, DeviceDetailsActivity.class);
            // 将可序列化的对象DeviceItem直接传入intent
            i.putExtra("deviceItem", deviceItem.get(position));
            startActivity(i);
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 调用搜索方法
                filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // 初始化完成后，先禁用所有组件，避免误触。
        enableAllComponent(false);
    }

    private void filter(String string) {
        List<DeviceItem> filteredList = new ArrayList<>();

        for (DeviceItem item : deviceItem) {
            // 如果DeviceItem包含搜索的文本，则添加到过滤列表中
            if (item.getDeviceName().toLowerCase().contains(string.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // 更新适配器
        deviceListAdapter.filterList(filteredList);
    }

    private void checkIsFirstTimeUse() {
        Log.d(TAG, "主界面-检查是否是第一次启动，checkIsFirstTimeUse called");
        enableAllComponent(false);
        // 显示加载进度条
        pb_devices_list.setVisibility(View.VISIBLE);
        boolean isFirstRun = sharedPreferences.getBoolean(getString(R.string.pref_first_run), true);
        boolean userAgreed = sharedPreferences.getBoolean(getString(R.string.pref_user_agreed), false);
        // todo:如果pref_user_token不为空，则尝试直接登录，期间禁用UI，登录成功则启用UI，失败则跳转登录界面
        Log.d(TAG, "主界面-checkIsFirstTimeUse读取到本地文件：isFirstRun:" + isFirstRun + "\nuserAgreed:" + userAgreed);
        if (isFirstRun && !userAgreed) {
            // UserAgreementActivity 是用户协议界面的Activity
            Log.d(TAG, "主界面-用户是第一次使用（从未登录过），且并未同意用户协议！");
            Intent intent = new Intent(this, UserAgreementActivity.class);
            startActivity(intent);
            finish(); // 关闭当前活动，以防用户返回到这个界面
        } else {
            enableAllComponent(true);
            // 显示加载进度条
            pb_devices_list.setVisibility(View.INVISIBLE);
        }

    }

    private void checkUserLoginStatus() {
        Log.d(TAG, "主界面-checkUserLoginStatus called");
        // 禁用所有组件，避免误触
        enableAllComponent(false);
        // 显示加载进度条
        pb_devices_list.setVisibility(View.VISIBLE);
        loginToken = sharedPreferences.getString(getString(R.string.pref_user_token), "");
        // todo:如果pref_user_token不为空，则尝试直接登录，期间禁用UI，登录成功则启用UI，失败则跳转登录界面
        Log.d(TAG, "主界面-checkUserLoginStatus读取到本地文件：loginToken:" + loginToken);
        if (LoginActivity.userLoggedIn && !loginToken.isEmpty()) {
            enableAllComponent(true);
            pb_devices_list.setVisibility(View.INVISIBLE);
        } else if (!LoginActivity.userLoggedIn && !loginToken.isEmpty()) {
            // 当登录标志为否，或本地未存储登录token时，重新验证登录
            Log.d(TAG, "主界面-用户登录标志为false或loginToken为空！开始服务器验证登录权限。");
            // todo:增加判断是否登录的逻辑。
            serverVerificationLoginToken(new CheckLoginStatueCallback() {
                @Override
                public void onSuccess(String newToken) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.pref_user_token), newToken);
                    editor.apply();
                    Log.d(TAG, "主界面-用户不是第一次使用，已同意用户协议，使用TOKEN登录成功并已保存！newToken：" + newToken);
                    // 启用所有组件，并隐藏进度条
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 确认登录成功后，启用所有组件，用户可以交互。
                            Log.d(TAG, "主界面-用户使用TOKEN登录成功！启用所有组件");
                            LoginActivity.userLoggedIn = true;
                            enableAllComponent(true);
                            pb_devices_list.setVisibility(View.INVISIBLE);
                        }
                    });

                }

                @Override
                public void onFailed() {
                    Log.e(TAG, "主界面-用户不是第一次使用，已同意用户协议，但是登录TOKEN不存在或过期！");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "用户未登录！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    loginToken = "";
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(getString(R.string.pref_user_token));
                    editor.apply();

                    LoginActivity.userLoggedIn = false;
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // 关闭当前活动，以防用户返回到这个界面
                }
            }, loginToken);
        } else {
            Log.d(TAG, "主界面-用户未登录！跳转登录界面");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // 关闭当前活动，以防用户返回到这个界面
        }
    }

    // 通过服务器校验用户登录状态
    private void serverVerificationLoginToken(CheckLoginStatueCallback checkLoginStatueCallback, String token) {
        Log.d(TAG, "主界面-serverVerificationLoginToken called");

        String uri = serverUrl + "/users/login";

        RequestBody body = RequestBody.create(new byte[0], null);

        Request request = new Request.Builder().url(uri).addHeader("Authorization", token).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "主界面-TOKEN登录失败，client-onFailure called!");
                checkLoginStatueCallback.onFailed();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                    String newToken = "";
                    Log.d(TAG, "主界面-TOKEN登录成功，response.code() == 200");
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        username = userObject.getString("username");
                        newToken = userObject.getString("token");

                        Log.d(TAG, "主界面-TOKEN登录返回体解析成功！newToken: " + newToken);

                        checkLoginStatueCallback.onSuccess(newToken);
                    } catch (JSONException e) {
                        Log.e(TAG, "主界面-TOKEN登录请求成功，但是返回值解析失败！");
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "登录失败，服务器返回数据格式错误，请稍后再试！", Toast.LENGTH_SHORT).show();
                            checkLoginStatueCallback.onFailed();
                        });
                    }
                } else {
                    Log.e(TAG, "主界面-onResponse-TOKEN登录请求失败！响应代码已获得：code:" + response.code());
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
                                Log.e(TAG, "主界面-onResponse-TOKEN登录请求失败！" + "响应体已经解析:" + message);
                            } catch (JSONException e) {
                                Log.e(TAG, "主界面-onResponse-TOKEN登录请求失败服务器响应体json解析失败！" + e);
                                // 添加错误处理代码
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "服务器返回的数据格式错误，请稍后再试", Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }
                    checkLoginStatueCallback.onFailed();
                }

            }
        });
    }

    // 更新界面列表为空时显示的文字
    private void updateEmptyState() {
        Log.d(TAG, "主界面-尝试更新列表是否为空的文本");
        if (tv_devices_list_empty != null && deviceItem != null) {
            if (deviceItem.isEmpty()) {
                tv_devices_list_empty.setVisibility(View.VISIBLE);
            } else {
                tv_devices_list_empty.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.e(TAG, "主界面-tv_devices_list_empty为null!");
        }
    }

    // 启用或停用所有设备
    private void enableAllComponent(boolean enable) {
        ib_shutdown.setEnabled(enable);
        ib_account.setEnabled(enable);
        ib_add_device.setEnabled(enable);
        et_search.setEnabled(enable);
        rv_devices_list.setEnabled(enable);
        deviceListAdapter.setClickable(enable);
    }

    // 响应界面组件点击事件
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_shutdown) {
            finish();
        } else if (view.getId() == R.id.ib_add_device) {
            Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivity(i);
        } else if (view.getId() == R.id.ib_account) {
            // TODO: 2024/7/20 完成用户界面，界面包括：用户账号名称、用户会员等级（后续推出充值界面）、用户退出登录按钮。 
            Intent i = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(i);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "主界面-onPause called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "主界面-onStop called!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "主界面-onDestroy called!");
    }

    // 当用户系统设置改变时，系统回调此函数（例如修改夜间模式等）
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "主界面-onConfigurationChanged called。newConfig:" + newConfig.orientation);
        // todo:处理配置变化
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时的处理
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏时的处理
        }
    }

    private interface CheckLoginStatueCallback {
        void onSuccess(String newToken);

        void onFailed();
    }

    public static class StoreCalibration {
        public static String device;
        public static byte[] storrefCoeff;
        public static byte[] storerefMatrix;
    }
}
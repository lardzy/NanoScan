package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class DeviceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DeviceDetailsActivity";
    private static String DEVICE_NAME = "NIR";
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private DeviceItem deviceItem;
    private TextView light_usage_duration, humidity_value, battery_level, tv_device_mac,
            tv_device_name, connect_text, spectral_reference_update_date_value, number_of_spectra_collected_value;
    private ProgressBar scan_progressbar, progressBar;
    private ImageView connect_btn, battery_image;
    private Animation fadeIn, fadeOut;
    private Handler handler;
    private boolean warmUp = false;
    private SharedPreferences sharedPreferences;
    // region 设备状态
    // todo:完成状态信息共享
    private int battery;
    private String totalLampTime = "";
    private String referenceUpdateDate = "-";
    private int numberOfSpectraCollected = 0;
    // endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "设备详情页-onCreate called!");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);
        // 初始化数据
        initialData();
        // 初始化组件
        initialComponent();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 更新设备状态信息、光谱数据。
        updateDeviceData();
    }

    private void initialComponent() {
        Log.d(TAG, "设备详情页-initialComponent called!");
        // 实例化组件
        not_preheat_toggle = findViewById(R.id.not_preheat_toggle);
        scan = findViewById(R.id.scan);
        device_connection_layout = findViewById(R.id.device_connection_layout);
        light_usage_duration = findViewById(R.id.light_usage_duration);
        battery_level = findViewById(R.id.battery_level);
        scan_progressbar = findViewById(R.id.scan_progressbar);
        progressBar = findViewById(R.id.progressBar);
        imageButton_back = findViewById(R.id.imageButton_back);
        imageButton_menu = findViewById(R.id.imageButton_menu);
        tv_device_mac = findViewById(R.id.tv_device_mac);
        tv_device_name = findViewById(R.id.tv_device_name);
        connect_btn = findViewById(R.id.connect_btn);
        battery_image = findViewById(R.id.battery_image);
        connect_text = findViewById(R.id.connect_text);
        spectral_reference_update_date_value = findViewById(R.id.spectral_reference_update_date_value);
        number_of_spectra_collected_value = findViewById(R.id.number_of_spectra_collected_value);

        tv_device_mac.setText(deviceItem.getDeviceMac());
        tv_device_name.setText(deviceItem.getDeviceName());

        scan.setEnabled(false);
        // 设置按钮点击事件
        scan.setOnClickListener(this);
        device_connection_layout.setOnClickListener(this);
        imageButton_back.setOnClickListener(this);
        imageButton_menu.setOnClickListener(this);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // 设置ToggleButton的监听事件，用于切换预热按钮的背景颜色和文字颜色
        not_preheat_toggle.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.d("DeviceDetailsActivity", "点击了预热按钮");
            int colorFrom = b ? Color.WHITE : Color.parseColor("#FFEACA");
            int colorTo = b ? Color.parseColor("#FFEACA") : Color.WHITE;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(300); // 动画持续时间，300毫秒
            colorAnimation.addUpdateListener(animator ->
                    not_preheat_toggle.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue())));
            colorAnimation.start();
            if (b) {
                not_preheat_toggle.setTextColor(Color.parseColor("#8CBEFF"));
                warmUp = true;
            } else {
                not_preheat_toggle.setTextColor(Color.GRAY);
                warmUp = false;
            }
        });
        updateDeviceData();
    }

    private int upDateBatteryIcon(int battery) {
        if (battery >= 0 && battery <= 12) {
            return R.drawable.baseline_battery_0_bar_24; // 0% - 12%
        } else if (battery >= 13 && battery <= 25) {
            return R.drawable.baseline_battery_1_bar_24; // 13% - 25%
        } else if (battery >= 26 && battery <= 37) {
            return R.drawable.baseline_battery_2_bar_24; // 26% - 37%
        } else if (battery >= 38 && battery <= 50) {
            return R.drawable.baseline_battery_3_bar_24; // 38% - 50%
        } else if (battery >= 51 && battery <= 62) {
            return R.drawable.baseline_battery_4_bar_24; // 51% - 62%
        } else if (battery >= 63 && battery <= 75) {
            return R.drawable.baseline_battery_5_bar_24; // 63% - 75%
        } else if (battery >= 76 && battery <= 87) {
            return R.drawable.baseline_battery_6_bar_24; // 76% - 87%
        } else if (battery >= 88 && battery <= 100) {
            return R.drawable.baseline_battery_full_24; // 88% - 100%
        } else {
            return R.drawable.baseline_battery_charging_full_24;
        }
    }

    // todo:读取本地的光谱、光源通电时长、参比日期、光谱数量更新设备信息并更新到界面。
    private void updateDeviceData() {
        device_connection_layout.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(fadeIn);
        connect_btn.setVisibility(View.GONE);
        connect_text.startAnimation(fadeOut);
        connect_text.setVisibility(View.GONE);
        // 读取本地数据
        battery = sharedPreferences.getInt(getString(R.string.pref_device_battery), -1);
        totalLampTime = sharedPreferences.getString(getString(R.string.pref_device_totalLampTime), "-");

        light_usage_duration.setText(totalLampTime);
        spectral_reference_update_date_value.setText(referenceUpdateDate);
        number_of_spectra_collected_value.setText(String.valueOf(numberOfSpectraCollected));
        if (battery > 0) {
            battery_level.setText(getString(R.string.battery_level, String.valueOf(battery) + "%"));
        } else {
            battery_level.setText(getString(R.string.not_available));
        }

        battery_image.setImageResource(upDateBatteryIcon(battery));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.startAnimation(fadeOut);
                        progressBar.setVisibility(View.GONE);
                        connect_btn.setVisibility(View.VISIBLE);
                        connect_text.setVisibility(View.VISIBLE);
                        connect_btn.startAnimation(fadeIn);
                        connect_text.startAnimation(fadeIn);
                        device_connection_layout.setClickable(true);
                        scan.setEnabled(true);
                    }
                });
            }
        }, 1000L);
    }

    private void initialData() {
        Log.d(TAG, "设备详情页-initialData called!");
        handler = new Handler();
        // 初始化数据
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        sharedPreferences = this.getSharedPreferences(Objects.requireNonNull(deviceItem).getDeviceMac(), Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan) {
            // todo:将是否预热灯源、设备deviceItem实例，传输给ScanViewActivity。
            // 点击了扫描按钮
            Log.d("DeviceDetailsActivity", "点击了扫描按钮");
            Intent intent = new Intent(DeviceDetailsActivity.this, ScanViewActivity.class);
            intent.putExtra("deviceItem", deviceItem);
            intent.putExtra("warmUp", warmUp);
            intent.putExtra("mainFlag", true);
            startActivity(intent);
        } else if (view.getId() == R.id.device_connection_layout) {
            // 点击了设备连接布局
            Log.d("DeviceDetailsActivity", "点击了设备连接布局");
            // 更新设备数据
            updateDeviceData();
        } else if (view.getId() == R.id.imageButton_back) {
            // 点击了返回按钮
            finish();
        } else if (view.getId() == R.id.imageButton_menu) {
            // 点击了菜单按钮
            Log.d("DeviceDetailsActivity", "点击了菜单按钮");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
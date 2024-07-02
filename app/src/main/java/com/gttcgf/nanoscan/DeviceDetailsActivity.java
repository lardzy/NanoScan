package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
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

public class DeviceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DeviceDetailsActivity";
    private static String DEVICE_NAME = "NIR";
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private DeviceItem deviceItem;
    private TextView light_usage_duration, temperature_value, humidity_value, battery_level, tv_device_mac, tv_device_name, connect_text;
    private ProgressBar scan_progressbar, progressBar;
    private ImageView connect_btn;
    private Animation fadeIn, fadeOut;
    private Handler handler;
    private boolean warmUp = false;


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

    private void initialComponent() {
        Log.d(TAG, "设备详情页-initialComponent called!");
        // 实例化组件
        not_preheat_toggle = findViewById(R.id.not_preheat_toggle);
        scan = findViewById(R.id.scan);
        device_connection_layout = findViewById(R.id.device_connection_layout);
        light_usage_duration = findViewById(R.id.light_usage_duration);
        temperature_value = findViewById(R.id.spectral_reference_update_date_value);
        humidity_value = findViewById(R.id.number_of_spectra_collected_value);
        battery_level = findViewById(R.id.battery_level);
        scan_progressbar = findViewById(R.id.scan_progressbar);
        progressBar = findViewById(R.id.progressBar);
        imageButton_back = findViewById(R.id.imageButton_back);
        imageButton_menu = findViewById(R.id.imageButton_menu);
        tv_device_mac = findViewById(R.id.tv_device_mac);
        tv_device_name = findViewById(R.id.tv_device_name);
        connect_btn = findViewById(R.id.connect_btn);
        connect_text = findViewById(R.id.connect_text);

        tv_device_mac.setText(deviceItem.getDeviceMac());
        tv_device_name.setText(deviceItem.getDeviceName());
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

    // 读取本地的光谱、光源通电时长、参比日期、光谱数量更新设备信息并更新到界面。
    private void updateDeviceData() {
        device_connection_layout.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(fadeIn);
        connect_btn.setVisibility(View.GONE);
        connect_text.startAnimation(fadeOut);
        connect_text.setVisibility(View.GONE);
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
                    }
                });
            }
        }, 3000L);
    }

    private void initialData() {
        Log.d(TAG, "设备详情页-initialData called!");
        handler = new Handler();
        // 初始化数据
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
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
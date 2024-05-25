package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private TextView light_usage_duration, temperature_value, humidity_value, battery_level;
    private ProgressBar scan_progressbar, progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);
        // 初始化组件
        initialComponent();
        // 初始化数据
        initialData(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initialComponent() {
        // 实例化组件
        not_preheat_toggle = findViewById(R.id.not_preheat_toggle);
        scan = findViewById(R.id.scan);
        device_connection_layout = findViewById(R.id.device_connection_layout);
        light_usage_duration = findViewById(R.id.light_usage_duration);
        temperature_value = findViewById(R.id.temperature_value);
        humidity_value = findViewById(R.id.humidity_value);
        battery_level = findViewById(R.id.battery_level);
        scan_progressbar = findViewById(R.id.scan_progressbar);
        progressBar = findViewById(R.id.progressBar);
        imageButton_back = findViewById(R.id.imageButton_back);
        imageButton_menu = findViewById(R.id.imageButton_menu);

        // 设置按钮点击事件
        scan.setOnClickListener(this);
        device_connection_layout.setOnClickListener(this);
        imageButton_back.setOnClickListener(this);
        imageButton_menu.setOnClickListener(this);

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
            } else {
                not_preheat_toggle.setTextColor(Color.GRAY);
            }
        });


    }

    private void initialData(Bundle savedInstanceState) {
        // 初始化数据
        
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan) {
            // 点击了扫描按钮
            Log.d("DeviceDetailsActivity", "点击了扫描按钮");
        } else if (view.getId() == R.id.device_connection_layout) {
            // 点击了设备连接布局
            Log.d("DeviceDetailsActivity", "点击了设备连接布局");
        } else if (view.getId() == R.id.imageButton_back) {
            // 点击了返回按钮
            finish();
        } else if (view.getId() == R.id.imageButton_menu) {
            // 点击了菜单按钮
            Log.d("DeviceDetailsActivity", "点击了菜单按钮");
        }
    }
}
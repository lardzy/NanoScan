package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ISCSDK.ISCNIRScanSDK;

public class DeviceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DeviceDetailsActivity";
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private DeviceItem deviceItem;
    private TextView light_usage_duration, temperature_value, humidity_value, battery_level, tv_device_mac, tv_device_name;
    private ProgressBar scan_progressbar, progressBar;
    private ISCNIRScanSDK mNanoBLEService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandle;

    public static String GetLampTimeString(long lamptime) {
        String lampusage = "";
        if (lamptime / 86400 != 0) {
            lampusage += lamptime / 86400 + "day ";
            lamptime -= 86400 * (lamptime / 86400);
        }
        if (lamptime / 3600 != 0) {
            lampusage += lamptime / 3600 + "hr ";
            lamptime -= 3600 * (lamptime / 3600);
        }
        if (lamptime / 60 != 0) {
            lampusage += lamptime / 60 + "min ";
            lamptime -= 60 * (lamptime / 60);
        }
        lampusage += lamptime + "sec ";
        return lampusage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);
        // 初始化数据
        initialData();
        // 初始化组件
        initialComponent();

        Intent gattServiceIntent = new Intent(this, ISCNIRScanSDK.class);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

                mNanoBLEService = ((ISCNIRScanSDK.LocalBinder) iBinder).getService();
                //初始化 bluetooth, 如果 BLE 不可用, 则 finish
                if (!mNanoBLEService.initialize()) {
                    finish();
                }

                BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (mBluetoothLeScanner == null) {
                    finish();
                    Toast.makeText(DeviceDetailsActivity.this, "请确保蓝牙已经打开！", Toast.LENGTH_SHORT).show();
                }
                mHandle = new Handler();

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

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
        tv_device_mac = findViewById(R.id.tv_device_mac);
        tv_device_name = findViewById(R.id.tv_device_name);

        tv_device_mac.setText(deviceItem.getDeviceMac());
        tv_device_name.setText(deviceItem.getDeviceName());
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

    private void initialData() {
        // 初始化数据
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        if (deviceItem != null) {
            Log.d(TAG, "设备详情页-获取到传入的设备对象：" + deviceItem.toString());
        } else {
            Log.e(TAG, "设备详情页-获取到传入的设备对象为NULL！");
            Toast.makeText(this, "无法获得设备信息，软件发生异常！", Toast.LENGTH_LONG).show();
            finish();
        }
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
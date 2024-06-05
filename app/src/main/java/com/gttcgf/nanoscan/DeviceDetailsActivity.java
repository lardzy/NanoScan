package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.getStringPref;
import static com.ISCSDK.ISCNIRScanSDK.storeStringPref;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
    private static String DEVICE_NAME = "NIR";
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private DeviceItem deviceItem;
    private TextView light_usage_duration, temperature_value, humidity_value, battery_level, tv_device_mac, tv_device_name;
    private ProgressBar scan_progressbar, progressBar;
    private ISCNIRScanSDK mNanoBLEService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    // 用户当前选择的设备的MAC地址
    private String preferredDevice;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "设备详情页-onServiceConnected called,服务已连接！");
            // 获得ISCNIRScanSDK服务对象
            mNanoBLEService = ((ISCNIRScanSDK.LocalBinder) iBinder).getService();
            //初始化 bluetooth, 如果 BLE 不可用, 则 finish
            if (!mNanoBLEService.initialize()) {
                Log.e(TAG, "设备详情页-BLE 不可用，活动结束！");
                finish();
            }

            BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner == null) {
                Log.e(TAG, "设备详情页-BluetoothLeScanner 不可用，活动结束！");
                finish();
                Toast.makeText(DeviceDetailsActivity.this, "请确保蓝牙已经打开！", Toast.LENGTH_SHORT).show();
            }
            mHandler = new Handler();
            // 如果存储的设备MAC不为空
            String deviceMac = getStringPref(DeviceDetailsActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (deviceMac != null) {
                preferredDevice = deviceMac;
                Log.d(TAG, "设备详情页-获取到存储的设备MAC！mac:" + deviceMac);
                scanPreferredLeDevice(true);
            } else {  // 如果存储的设备MAC为空
                Log.e(TAG, "设备详情页-存储的设备MAC为空!");
                scanLeDevice(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mNanoBLEService = null;
        }
    };
    private AlertDialog alertDialog;
    // 设备是否已经连接上
    private boolean connected;
    @SuppressLint("MissingPermission")
    private final ScanCallback mPreferredLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "设备详情页-mPreferredLeScanCallback的onScanResult被调用! result:" + result.toString());
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String preferredNano = getStringPref(DeviceDetailsActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (preferredNano != null && name != null) {
                // 设备名称包含设备前缀(DEVICE_NAME)，且设备MAC地址等于选中设备的MAC地址
                if (name.contains(DEVICE_NAME) && device.getAddress().equals(preferredNano)) {
                    // 连接当前的设备
                    Log.d(TAG, "设备详情页-mPreferredLeScanCallback的onScanResult 成功获取preferredNano和name"
                            + preferredNano + "，name:" + name + "。正式开始连接设备！connected = true");
                    mNanoBLEService.connect(device.getAddress());
                    connected = true;
                    scanPreferredLeDevice(false);
                }
            } else {
                Log.e(TAG, "设备详情页面-mPreferredLeScanCallback的onScanResult中，" +
                        "preferredNano或name为null! preferredNano:" + preferredNano + "，name:" + name);
            }
        }
    };
    @SuppressLint("MissingPermission")
    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "设备详情页-mLeScanCallback的onScanResult被调用! result:" + result.toString());
            BluetoothDevice device = result.getDevice();
            String preferredNano = getStringPref(DeviceDetailsActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (preferredNano != null) {
                if (preferredNano.equals(device.getAddress())) {
                    Log.d(TAG, "设备详情页-mLeScanCallback的onScanResult ，preferredNano和device.getAddress()一致（mac一致）！"
                            + preferredNano + "，device.getAddress:" + device.getAddress() + "。正式开始连接设备！");
                    mNanoBLEService.connect(preferredNano);
                    connected = true;
                    scanLeDevice(false);
                }
            }
        }
    };

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
        Log.d(TAG, "设备详情页-onCreate called!");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);
        // 初始化数据
        initialData();
        // 初始化组件
        initialComponent();
        // 绑定ISCNIRScanSDK服务
        Intent gattServiceIntent = new Intent(this, ISCNIRScanSDK.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "设备详情页-ISCNIRScanSDK服务已绑定!");
        //todo: region 注册广播接收器


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
        Log.d(TAG, "设备详情页-initialData called!");
        // 初始化数据
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        if (deviceItem != null) {
            Log.d(TAG, "设备详情页-获取到传入的设备对象：" + deviceItem.toString());
            // 使用SDK中的方法，存储选中的设备信息，包括设备mac和名称
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceItem.getDeviceMac());
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, deviceItem.getDeviceName());

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

    // 开始优先扫描用户选择的蓝牙设备
    @SuppressLint("MissingPermission")
    private void scanPreferredLeDevice(final boolean enable) {
        Log.d(TAG, "设备详情页-scanPreferredLeDevice called，开始优先扫描用户选择的蓝牙设备！enable:" + enable);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 停止扫描
                    Log.e(TAG, "设备详情页-mHandler.postDelayed，超时， mBluetoothLeScanner.stopScan被调用以停止扫描！（mPreferredLeScanCallback）");
                    mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
                    if (!connected) {
                        // 如果6秒后还没有连接上，则扫描任何名称中包含 "NIR" 的蓝牙设备
                        Log.e(TAG, "设备详情页-mHandler.postDelayed，超时且connected为" + false + "。准备调用scanLeDevice直接扫描MAC地址相同的设备！");
                        scanLeDevice(true);
                    }
                }
            }, ISCNIRScanSDK.SCAN_PERIOD);
            if (mBluetoothLeScanner == null) {
                // 提示用户打开蓝牙
                Log.e(TAG, "设备详情页-mBluetoothLeScanner为null，用户蓝牙未启动!");
                notConnectedDialog();
            } else {
                // 开始扫描并传入回调接口实现类
                Log.d(TAG, "设备详情页-mBluetoothLeScanner.startScan(mPreferredLeScanCallback)调用，开始扫描用户指定的设备！");
                mBluetoothLeScanner.startScan(mPreferredLeScanCallback);
            }
        } else {
            // 停止扫描
            Log.d(TAG, "设备详情页-scanPreferredLeDevice called，停止扫描！enable:" + false);
            mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    // 开始扫描所有前缀符合的蓝牙设备
    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "设备详情页-scanLeDevice called，开始扫描用户指定的设备！");
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothLeScanner != null) {
                        Log.e(TAG, "设备详情页-scanLeDevice的mHandler.postDelayed超时， mBluetoothLeScanner.stopScan被调用以停止扫描！（mLeScanCallback）");
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        if (!connected) {
                            Log.e(TAG, "设备详情页-mHandler.postDelayed，超时且connected为" + false + "。准备调用notConnectedDialog()退出！");
                            notConnectedDialog();
                        }
                    }
                }
            }, ISCNIRScanSDK.SCAN_PERIOD);
            // 开始扫描
            if (mBluetoothLeScanner == null) {
                Log.e(TAG, "设备详情页-mBluetoothLeScanner为null，用户蓝牙未启动!");
                notConnectedDialog();
            } else {
                Log.d(TAG, "设备详情页-mBluetoothLeScanner.startScan(mLeScanCallback)调用，开始扫所有mac相同的设备！");
                mBluetoothLeScanner.startScan(mLeScanCallback);
            }
        } else {
            Log.d(TAG, "设备详情页-scanLeDevice called，停止扫描！enable:" + false);
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    private void notConnectedDialog() {
        Log.e(TAG, "设备详情页-notConnectedDialog called，设备连接失败，弹窗提示用户!");
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.not_connected_title));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(this.getResources().getString(R.string.not_connected_message));
        // 用户点击确认后，结束当前activity
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), (arg0, arg1) -> {
            alertDialog.dismiss();
            finish();
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }




}
package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.storeStringPref;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ISCSDK.ISCNIRScanSDK;

import java.util.ArrayList;

public class SelectDeviceViewActivity extends AppCompatActivity implements View.OnClickListener { // 选择蓝牙设备界面
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = getRequiredPermissions();
    private static String DEVICE_NAME = "NIR";
    public BluetoothLeScanner mBluetoothLeScanner;
    private ImageButton imageButton_back;
    private ListView lv_nanoDevices;
    private Context context;
    private Handler handler;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback scannerCallback;
    private ArrayList<ISCNIRScanSDK.NanoDevice> nanoDeviceList = new ArrayList<>();
    private NanoScanAdapter nanoScanAdapter;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private String pref_user_phone_number, pref_user_password, pref_user_token, pref_user_ipAddress;

    // 需要的权限列表，获取位置和蓝牙相关权限。
    private static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else { // Android 6.0 到 Android 11
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_device_view);
        this.context = this;
        if (!allPermissionsGranted()) {
            Toast.makeText(SelectDeviceViewActivity.this, "请授予权限以连接设备！", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            finish();
            return;
        }
        initialData();
        initialComponent();

        // 获取 BluetoothLeScanner 实例
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        } else {
            Toast.makeText(this, "蓝牙未启用！", Toast.LENGTH_SHORT).show();
        }

        nanoScanAdapter = new NanoScanAdapter(this, nanoDeviceList);
        lv_nanoDevices.setAdapter(nanoScanAdapter);

        lv_nanoDevices.setOnItemClickListener((adapterView, view, i, l) ->
                confirmationDialog(nanoDeviceList.get(i).getNanoMac(), nanoDeviceList.get(i).getNanoName()));

        // 实例化handle
        handler = new Handler();

        // 扫描设备
        scanLeDevice(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initialData() {
        DEVICE_NAME = ISCNIRScanSDK.getStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.DeviceFilter, "NIR");
        sharedPreferences = getSharedPreferences("default", MODE_PRIVATE);
        pref_user_phone_number = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        pref_user_password = sharedPreferences.getString(getString(R.string.pref_user_password), "");
        pref_user_token = sharedPreferences.getString(getString(R.string.pref_user_token), "");
        pref_user_ipAddress = sharedPreferences.getString(getString(R.string.pref_user_ipAddress), "");
    }

    private void initialComponent() {
        imageButton_back = findViewById(R.id.imageButton_back);
        lv_nanoDevices = findViewById(R.id.lv_nanoDevices);
        imageButton_back.setOnClickListener(this);


        // 设置扫描回调
        scannerCallback = new ScanCallback() {
            // 处理单个扫描结果
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

                // 如果扫描到的设备名称不为null，且名称中包含设定的名称前缀，且getScanRecord对象不为null!
                @SuppressLint("MissingPermission")
                String name = device.getName();
                if (name != null && name.contains(DEVICE_NAME) && result.getScanRecord() != null) {
                    Boolean isDeviceInList = false;
                    // 新建ISCNIRScanSDK.NanoDevice对象
                    ISCNIRScanSDK.NanoDevice nanoDevice = new ISCNIRScanSDK.NanoDevice(device, result.getRssi(), result.getScanRecord().getBytes());
                    // 判断设备是否已在列表中，如果已经在，就该设备更新信号强度。
                    for (ISCNIRScanSDK.NanoDevice d : nanoDeviceList) {
                        if (d.getNanoMac().equals(device.getAddress())) {
                            isDeviceInList = true;
                            d.setRssi(result.getRssi());
                            nanoScanAdapter.notifyDataSetChanged();
                        }
                    }
                    // 如果不在设备列表中，则添加到设备集合中，并通知列表更新。
                    if (!isDeviceInList) {
                        nanoDeviceList.add(nanoDevice);
                        nanoScanAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice(boolean enable) {
        if (mBluetoothLeScanner == null) {
            Toast.makeText(this, "蓝牙未启用！", Toast.LENGTH_SHORT);
        } else {
            if (enable) {
                handler.postDelayed(() -> mBluetoothLeScanner.stopScan(scannerCallback), ISCNIRScanSDK.SCAN_PERIOD);  // 6000L
                mBluetoothLeScanner.startScan(scannerCallback);
            } else {
                mBluetoothLeScanner.startScan(scannerCallback);
            }
        }
    }

    // 显示弹窗并验证
    public void confirmationDialog(String mac, final String name) {
        Bundle bundle = new Bundle();
        bundle.putString("username", pref_user_phone_number);
        bundle.putString("password", pref_user_password);
        bundle.putString("pcode", pref_user_ipAddress);
        bundle.putString("mcode", mac);
        bundle.putString("token", pref_user_token);

        DevicePermissionCheckFragment checkFragment = DevicePermissionCheckFragment.newInstance(bundle, new DevicePermissionCheckFragment.VerifyDevicePermissionCallback() {
            @Override
            public void onSuccess(String token) {
                if (!token.isEmpty()) {
                    final String deviceMac = mac;
                    // 使用SDK中的方法，存储选中的设备信息，包括设备mac和名称
                    storeStringPref(context, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceMac);
                    storeStringPref(context, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, name);
                    Intent i = new Intent();
                    i.putExtra("NAME", name);
                    i.putExtra("MAC", mac);
                    i.putExtra("DEVICE_TOKEN", token);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            }

            @Override
            public void onFailed() {

            }
        });
        checkFragment.show(getSupportFragmentManager(), "DevicePermissionCheckFragment");

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageButton_back) {
            finish();
        }
    }

    // 检查所有权限
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
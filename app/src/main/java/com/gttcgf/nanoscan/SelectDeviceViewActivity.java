package com.gttcgf.nanoscan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ISCSDK.ISCNIRScanSDK;
import com.gttcgf.nanoscan.data.model.UserSession;
import com.gttcgf.nanoscan.databinding.ActivitySelectDeviceViewBinding;
import com.gttcgf.nanoscan.viewmodel.SelectDeviceViewModel;

import java.util.ArrayList;

public class SelectDeviceViewActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SelectDeviceViewActivit";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = getRequiredPermissions();
    private static String DEVICE_NAME = "NIR";

    private ActivitySelectDeviceViewBinding binding;
    private SelectDeviceViewModel viewModel;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private ScanCallback scannerCallback;
    private final ArrayList<ISCNIRScanSDK.NanoDevice> nanoDeviceList = new ArrayList<>();
    private NanoScanAdapter nanoScanAdapter;
    private boolean isStopped = false;

    private static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySelectDeviceViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SelectDeviceViewModel.class);
        if (!allPermissionsGranted()) {
            Toast.makeText(this, "请授予权限以连接设备！", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            finish();
            return;
        }

        DEVICE_NAME = ISCNIRScanSDK.getStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.DeviceFilter, "NIR");
        handler = new Handler();
        initComponent();
        initBluetooth();
        observeViewModel();
        viewModel.onScanStarted();
        scanLeDevice(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initComponent() {
        binding.imageButtonBack.setOnClickListener(this);
        nanoScanAdapter = new NanoScanAdapter(this, nanoDeviceList);
        binding.lvNanoDevices.setAdapter(nanoScanAdapter);
        binding.lvNanoDevices.setOnItemClickListener((adapterView, view, position, l) ->
                confirmationDialog(nanoDeviceList.get(position).getNanoMac(), nanoDeviceList.get(position).getNanoName()));

        scannerCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                @SuppressLint("MissingPermission")
                String name = device.getName();
                if (name != null && name.contains(DEVICE_NAME) && result.getScanRecord() != null) {
                    ISCNIRScanSDK.NanoDevice nanoDevice = new ISCNIRScanSDK.NanoDevice(device, result.getRssi(), result.getScanRecord().getBytes());
                    viewModel.onDeviceFound(nanoDevice);
                }
            }
        };
    }

    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            return;
        }
        Toast.makeText(this, "蓝牙未启用！", Toast.LENGTH_SHORT).show();
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            nanoDeviceList.clear();
            nanoDeviceList.addAll(state.getDevices());
            nanoScanAdapter.notifyDataSetChanged();
            binding.pbLoadDevice.setVisibility(state.isLoading() ? View.VISIBLE : View.INVISIBLE);
            binding.tvLoading.setVisibility(state.isLoading() ? View.VISIBLE : View.INVISIBLE);
        });

        viewModel.getEvents().observe(this, event -> {
            SelectDeviceViewModel.SelectDeviceAction action = event != null ? event.getContentIfNotHandled() : null;
            if (action == null) {
                return;
            }
            switch (action.getType()) {
                case SHOW_TIMEOUT_DIALOG:
                    if (!isFinishing() && !isDestroyed() && !isStopped) {
                        GeneralMessageDialogFragment messageDialogFragment = GeneralMessageDialogFragment.newInstance(
                                GeneralMessageDialogFragment.MESSAGE_TYPE_ERROR,
                                getString(R.string.scanning_bluetooth_device_timeout_title),
                                getString(R.string.scanning_bluetooth_device_timeout_content)
                        );
                        messageDialogFragment.show(getSupportFragmentManager(), "DeviceScanTimeout");
                    } else {
                        finish();
                    }
                    break;
                case SHOW_TOAST:
                    Toast.makeText(this, action.getMessage(), Toast.LENGTH_LONG).show();
                    break;
                case FINISH_WITH_RESULT:
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("NAME", action.getDeviceName());
                    resultIntent.putExtra("MAC", action.getMacAddress());
                    resultIntent.putExtra("DEVICE_TOKEN", action.getToken());
                    setResult(Activity.RESULT_OK, resultIntent);
                    GeneralMessageDialogFragment messageDialogFragment = GeneralMessageDialogFragment.newInstance(
                            GeneralMessageDialogFragment.MESSAGE_TYPE_CHECK,
                            getString(R.string.device_added_successfully),
                            getString(R.string.device_added_successfully_content, action.getDeviceName())
                    );
                    messageDialogFragment.show(getSupportFragmentManager(), "Device added successfully");
                    break;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice(boolean enable) {
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "蓝牙未启用！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (enable) {
            handler.postDelayed(() -> {
                bluetoothLeScanner.stopScan(scannerCallback);
                viewModel.onScanFinished(isStopped);
            }, ISCNIRScanSDK.SCAN_PERIOD);
            Toast.makeText(this, "扫描蓝牙设备中...", Toast.LENGTH_LONG).show();
            bluetoothLeScanner.startScan(scannerCallback);
            return;
        }
        bluetoothLeScanner.stopScan(scannerCallback);
        viewModel.onScanFinished(isStopped);
    }

    public void confirmationDialog(String mac, final String name) {
        UserSession session = viewModel.getCurrentSession();
        Bundle bundle = new Bundle();
        bundle.putString("username", session.getPhoneNumber());
        bundle.putString("password", session.getPassword());
        bundle.putString("pcode", session.getIpAddress());
        bundle.putString("mcode", mac);
        bundle.putString("token", session.getToken());

        DevicePermissionCheckFragment checkFragment = DevicePermissionCheckFragment.newInstance(bundle, new DevicePermissionCheckFragment.VerifyDevicePermissionCallback() {
            @Override
            public void onSuccess(String token) {
                if (!token.isEmpty()) {
                    viewModel.onDeviceAuthorized(name, mac, token);
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

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

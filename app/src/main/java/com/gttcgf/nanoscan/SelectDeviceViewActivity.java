package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.storeStringPref;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;

import android.Manifest;
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

public class SelectDeviceViewActivity extends AppCompatActivity implements View.OnClickListener {
    private static String DEVICE_NAME = "NIR";
    public BluetoothLeScanner mBluetoothLeScanner;
    private ImageButton imageButton_back;
    private ListView lv_nanoDevices;
    private Context context;
    private Handler handler;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<ISCNIRScanSDK.NanoDevice> nanoDeviceList = new ArrayList<>();
    private NanoScanAdapter nanoScanAdapter;
    private AlertDialog alertDialog;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = getRequiredPermissions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_device_view);
        this.context = this;
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        initialData();
        initialComponent();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        nanoScanAdapter = new NanoScanAdapter(this, nanoDeviceList);
        lv_nanoDevices.setAdapter(nanoScanAdapter);
        lv_nanoDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmationDialog(nanoDeviceList.get(i).getNanoMac(), nanoDeviceList.get(i).getNanoName());
            }
        });
        handler = new Handler();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initialData() {
        DEVICE_NAME = ISCNIRScanSDK.getStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.DeviceFilter, "NIR");

    }

    private void initialComponent() {
        imageButton_back = findViewById(R.id.imageButton_back);
        lv_nanoDevices = findViewById(R.id.lv_nanoDevices);

        imageButton_back.setOnClickListener(this);

    }

    private void scanLeDevice(boolean enable) {
        if (mBluetoothLeScanner == null) {
            Toast.makeText(this, "蓝牙未启用！", Toast.LENGTH_SHORT);
        } else {
            if (enable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, ISCNIRScanSDK.SCAN_PERIOD);
            }
        }
    }

    public void confirmationDialog(String mac, final String name) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final String deviceMac = mac;
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.nano_confirmation_title));
        alertDialogBuilder.setMessage(this.getResources().getString(R.string.nano_confirmation_msg, mac));

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), (arg0, arg1) -> {
            alertDialog.dismiss();
            storeStringPref(context, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceMac);
            storeStringPref(context, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, name);
            finish();
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
        } else { // Android 6.0 to Android 11
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

    }

}
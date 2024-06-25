package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.getStringPref;
import static com.ISCSDK.ISCNIRScanSDK.storeStringPref;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ISCSDK.ISCNIRScanSDK;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ScanViewActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ScanViewActivity";
    private static String DEVICE_NAME = "NIR";
    private Context mContext;

    // region broadcast 接收器、过滤器
    private final BroadcastReceiver GetDeviceStatusReceiver = new GetDeviceStatusReceiver();
    private final BroadcastReceiver RefCoeffDataProgressReceiver = new RefCoeffDataProgressReceiver();
    private final BroadcastReceiver RefDataReadyReceiver = new RefDataReadyReceiver();
    private final BroadcastReceiver CalMatrixDataProgressReceiver = new CalMatrixDataProgressReceiver();
    private final BroadcastReceiver NotifyCompleteReceiver = new NotifyCompleteReceiver();
    private final BroadcastReceiver ReturnSetLampReceiver = new ReturnSetLampReceiver();
    private final BroadcastReceiver GetActiveScanConfReceiver = new GetActiveScanConfReceiver();
    private final IntentFilter requestCalCoeffFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_COEFF);
    private final IntentFilter refReadyFilter = new IntentFilter(ISCNIRScanSDK.REF_CONF_DATA);
    private final IntentFilter notifyCompleteFilter = new IntentFilter(ISCNIRScanSDK.ACTION_NOTIFY_DONE);
    private final IntentFilter requestCalMatrixFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_MATRIX);

    // endregion
    private boolean warmUp = false;
    private LampInfo lampInfo = LampInfo.ManualLamp;
    private ArrayList<ISCNIRScanSDK.ScanConfiguration> ScanConfigList = new ArrayList<>();
    // region UI组件
    private FrameLayout view_back;
    private Button start_scan_button;
    private ProgressBar pb_load_calibration;
    private TextView tv_load_calibration;
    private LineChart chart;
    private RecyclerView rv_function_list;
    private FunctionListAdapter functionListAdapter;
    private List<FunctionItem> functionList = new ArrayList<>();
    private Animation fadeIn, fadeOut;
    // endregion
    private DeviceItem deviceItem;
    private ISCNIRScanSDK mNanoBLEService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    // 当前选择的设备的MAC地址
    private String preferredDevice;
    private Handler mHandler;
    private AlertDialog alertDialog;
    // region 设备的校准系数和矩阵
    private byte[] refCoeff;
    private byte[] refMatrix;
    int refCoeffDataProgressTotalSize = 0;
    int refCoeffDataProgressCurrentProgress = 0;
    // endregion
    // 设备是否已经连接上
    private boolean connected;
    // region 设备配置文件
    // 存储设备配置文件数量
    private int storedConfSize;
    // 记录已经接收到的配置文件内容数量
    private int receivedConfSize = 0;
    // 记录扫描配置字节列表
    private ArrayList<byte[]> ScanConfig_Byte_List = new ArrayList<>();
    // 从 scan configuration page 页面获取的记录扫描配置字节列表
    private ArrayList<byte[]> ScanConfig_Byte_List_from_ScanConfiuration = new ArrayList<>();
    private int ActiveConfigIndex;
    // 设备当前活动的扫描配置
    private ISCNIRScanSDK.ScanConfiguration activeConf;
    // 存储当前活动配置的字节数组
    private byte[] ActiveConfigByte;
    // endregion
    // region GetDeviceStatusReceiver使用的变量、常量。
    private String battery = "";
    private String TotalLampTime = "";
    private byte[] devbyte;
    private byte[] errbyte;
    private float temprature;
    private float humidity;
    // endregion

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
        Log.e(TAG, "扫描页-onCreate called.");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_view);
        mContext = this;
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialData();
        initialComponent();
        // 绑定服务
        Intent intent = new Intent(this, ISCNIRScanSDK.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "扫描页-ISCNIRScanSDK服务已绑定!");
        //todo: region 注册广播接收器
        //region Register all needed broadcast receivers
        Log.d(TAG, "扫描页-开始注册广播。");
        LocalBroadcastManager.getInstance(this).registerReceiver(GetDeviceStatusReceiver, new IntentFilter(ISCNIRScanSDK.ACTION_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(RefCoeffDataProgressReceiver, requestCalCoeffFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(NotifyCompleteReceiver, notifyCompleteFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(CalMatrixDataProgressReceiver, requestCalMatrixFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(RefDataReadyReceiver, refReadyFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnSetLampReceiver, new IntentFilter(ISCNIRScanSDK.SET_LAMPSTATE_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(GetActiveScanConfReceiver, new IntentFilter(ISCNIRScanSDK.SEND_ACTIVE_CONF));
        // endregion
    }

    private void initialComponent() {
        view_back = findViewById(R.id.view_back);
        start_scan_button = findViewById(R.id.start_scan_button);
        pb_load_calibration = findViewById(R.id.pb_load_calibration);
        tv_load_calibration = findViewById(R.id.tv_load_calibration);
        rv_function_list = findViewById(R.id.rv_function_list);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        start_scan_button.setEnabled(false);

        // 初始化功能列表
        rv_function_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        functionListAdapter = new FunctionListAdapter(this, functionList);
        rv_function_list.setAdapter(functionListAdapter);
    }

    // 初始化各类数据
    private void initialData() {
        // todo: 后续根据是否存储了参比数据判断要不要默认选择使用出厂参比
        // 初始化功能列表
        FunctionItem functionItem_1 = new FunctionItem("采集模式", "采集并扫描", R.drawable.baseline_auto_graph_24, true);
        functionItem_1.setSelected(true);
        functionList.add(functionItem_1);
        functionList.add(new FunctionItem("采集模式", "仅采集光谱", R.drawable.baseline_stacked_line_chart_24, true));
        functionList.add(new FunctionItem("设备功能", "更新参比", R.drawable.baseline_switch_access_shortcut_24, true));
        // 使用出厂参比可以与其他选项共存
        FunctionItem functionItem_2 = new FunctionItem("设备功能", "使用出厂参比", R.drawable.baseline_factory_24, false);
        functionItem_2.setSelected(true);
        functionList.add(functionItem_2);

        // 获取传入的设备对象deviceItem
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        warmUp = getIntent().getBooleanExtra("warmUp", false);
        Log.d(TAG, "扫描页-获取到deviceItem：" + deviceItem + "\n" + "获取到warmUp：" + warmUp);
        if (deviceItem != null) {
            Log.d(TAG, "扫描页-获取到传入的设备对象，准备存储DeviceMac、DeviceName到SDK：" + deviceItem);
            // 使用SDK中的方法，存储选中的设备信息，包括设备mac和名称
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceItem.getDeviceMac());
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, deviceItem.getDeviceName());
        } else {
            Log.e(TAG, "扫描页-获取到传入的设备对象为NULL！");
            Toast.makeText(this, "无法获得设备信息，软件发生异常！", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.view_back) {
            finish();
        } else if (view.getId() == R.id.start_scan_button) {
            Toast.makeText(this, "点击了扫描按钮", Toast.LENGTH_SHORT).show();
        }
    }

    // 开始优先扫描用户选择的蓝牙设备
    @SuppressLint("MissingPermission")
    private void scanPreferredLeDevice(final boolean enable) {
        Log.d(TAG, "扫描页-scanPreferredLeDevice called，开始优先扫描用户选择的蓝牙设备！enable:" + enable);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 停止扫描
                    Log.e(TAG, "扫描页-mHandler.postDelayed，超时， mBluetoothLeScanner.stopScan被调用以停止扫描！（mPreferredLeScanCallback）");
                    mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
                    if (!connected) {
                        // 如果6秒后还没有连接上，则扫描任何名称中包含 "NIR" 的蓝牙设备
                        Log.e(TAG, "扫描页-mHandler.postDelayed，超时且connected为" + false + "。准备调用scanLeDevice直接扫描MAC地址相同的设备！");
                        scanLeDevice(true);
                    }
                }
            }, ISCNIRScanSDK.SCAN_PERIOD);
            if (mBluetoothLeScanner == null) {
                // 提示用户打开蓝牙
                Log.e(TAG, "扫描页-mBluetoothLeScanner为null，用户蓝牙未启动!");
                notConnectedDialog();
            } else {
                // 开始扫描并传入回调接口实现类
                Log.d(TAG, "扫描页-mBluetoothLeScanner.startScan(mPreferredLeScanCallback)调用，开始扫描用户指定的设备！");
                mBluetoothLeScanner.startScan(mPreferredLeScanCallback);
            }
        } else {
            // 停止扫描
            Log.d(TAG, "扫描页-scanPreferredLeDevice called，停止扫描！enable:" + false);
            mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    // 开始扫描所有前缀符合的蓝牙设备
    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "扫描页-scanLeDevice called，开始扫描用户指定的设备！");
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothLeScanner != null) {
                        Log.e(TAG, "扫描页-scanLeDevice的mHandler.postDelayed超时， mBluetoothLeScanner.stopScan被调用以停止扫描！（mLeScanCallback）");
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        if (!connected) {
                            Log.e(TAG, "扫描页-mHandler.postDelayed，超时且connected为" + false + "。准备调用notConnectedDialog()退出！");
                            notConnectedDialog();
                        }
                    }
                }
            }, ISCNIRScanSDK.SCAN_PERIOD);
            // 开始扫描
            if (mBluetoothLeScanner == null) {
                Log.e(TAG, "扫描页-mBluetoothLeScanner为null，用户蓝牙未启动!");
                notConnectedDialog();
            } else {
                Log.d(TAG, "扫描页-mBluetoothLeScanner.startScan(mLeScanCallback)调用，开始扫所有mac相同的设备！");
                mBluetoothLeScanner.startScan(mLeScanCallback);
            }
        } else {
            Log.d(TAG, "扫描页-scanLeDevice called，停止扫描！enable:" + false);
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    private void notConnectedDialog() {
        Log.e(TAG, "扫描页-notConnectedDialog called，设备连接失败，弹窗提示用户!");
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.not_connected_title));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(this.getResources().getString(R.string.not_connected_message));
        // 用户点击确认后，结束当前activity
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), (arg0, arg1) -> {
            alertDialog.dismiss();
            // todo: 调试期间注释finish
//            finish();
        });
        // 确保此时用户没有退出当前Activity
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

        }
    }

    public enum LampInfo {
        WarmDevice, ManualLamp, CloseWarmUpLampInScan
    }

    // 灯源设置完成会发送对应广播，接收器根据灯源状态进行进一步的操作
    public class ReturnSetLampReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Complete set lamp on,off,auto
            switch (lampInfo) {
                case ManualLamp:
                    break;
                case WarmDevice:
                    // 设备预热完成，将 Lamp_Info 设置为 ManualLamp，然后继续后续操作
                    lampInfo = LampInfo.ManualLamp;

                    // 当更新过设备出厂参比，则将存储的ReferenceScan改回"Not"。
                    if (getStringPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.ReferenceScan, "Not").equals("ReferenceScan"))
                        storeStringPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.ReferenceScan, "Not");

                    //Synchronize time and download calibration coefficient and calibration matrix
                    ISCNIRScanSDK.SetCurrentTime();
                    break;
                case CloseWarmUpLampInScan:
                    warmUp = false;
                    ISCNIRScanSDK.GetDeviceStatus();
                    break;
            }
        }
    }

    public class NotifyCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // todo:如果用户本次连接已经存储过校准系数，则再次进入此界面时，不需要重新再获取校准系数和矩阵
            //  （ ISCNIRScanSDK.ShouldDownloadCoefficient = false）。
            Log.d(TAG, "扫描页-NotifyCompleteReceiver called.\nwarmUp:" + warmUp);
            // 如果用户选择了预热设备
            if (warmUp) {
                ISCNIRScanSDK.ControlLamp(ISCNIRScanSDK.LampState.ON);
                lampInfo = LampInfo.WarmDevice;
                // 注册ReturnSetLampReceiver以执行下一步
            } else {
                // 判断当前用户是不是刚刚替换了“出厂参比”，如果替换了，则确保用户不会跳过获取校准矩阵、校准参数的步骤
                boolean reference = false;
                if (getStringPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.ReferenceScan, "Not").equals("ReferenceScan")) {
                    Log.e(TAG, "扫描页-NotifyCompleteReceiver用户替换设备出厂参比后重新进入扫描页");
                    reference = true;
                }
                // todo:增加判断如果被中断，存储的校准系数和矩阵不完全，则不读取本地数据
                // 当软件本次启动期间，已经存储过校准系数和矩阵，则跳过校准并将ISCNIRScanSDK.ShouldDownloadCoefficient设置为false。
                if (preferredDevice.equals(MainActivity.StoreCalibration.device) && !reference) {
                    Log.d(TAG, "扫描页-NotifyCompleteReceiver用户在本地已经存储校准参数");
                    refCoeff = MainActivity.StoreCalibration.storrefCoeff;
                    refMatrix = MainActivity.StoreCalibration.storerefMatrix;
                    ArrayList<ISCNIRScanSDK.ReferenceCalibration> refCal = new ArrayList<>();
                    refCal.add(new ISCNIRScanSDK.ReferenceCalibration(refCoeff, refMatrix));
                    ISCNIRScanSDK.ReferenceCalibration.writeRefCalFile(mContext, refCal);
                    // todo:此处可以初始化进度条
                    //获取 active config ，同步时间。
                    ISCNIRScanSDK.ShouldDownloadCoefficient = false;  // 确保SDK不会下载校准数据，直接进入下一步
                    ISCNIRScanSDK.SetCurrentTime();
                    tv_load_calibration.setText(getString(R.string.calibration_coefficient_and_matrix_read_from_local));
                } else {
                    // 本次启动没有存储校准参数，同步时间并下载校准系数和校准矩阵
                    ISCNIRScanSDK.ShouldDownloadCoefficient = true;
                    ISCNIRScanSDK.SetCurrentTime();
                    Log.d(TAG, "扫描页-NotifyCompleteReceiver本地未找到校准数据，NotifyCompleteReceiver SetCurrentTime() called.");
                }

            }
        }
    }

    public class GetDeviceStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-GetDeviceStatusReceiver接收到广播！");
            battery = Integer.toString(intent.getIntExtra(ISCNIRScanSDK.EXTRA_BATT, 0));
            long lamptime = intent.getLongExtra(ISCNIRScanSDK.EXTRA_LAMPTIME, 0);
            TotalLampTime = GetLampTimeString(lamptime);
            devbyte = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DEV_STATUS_BYTE);
            Log.e(TAG, "battery:" + battery + "TotalLampTime:" + TotalLampTime + "devbyte:" + Arrays.toString(devbyte));
        }
    }

    // 此广播接收器可以查看校准系数的接收进度，ACTION_REQ_CAL_COEFF(ISCNIRScanSDK.SetCurrentTime()must be called)
    public class RefCoeffDataProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取总大小
            int intExtra = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0);
            Log.d(TAG, "扫描页-RefCoeffDataProgressReceiver-onReceive-intExtra，接受到EXTRA_REF_CAL_COEFF_SIZE为：" + intExtra);
            //  Boolean size代表是否是第一个数据包、且ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE是否是代表总大小
            boolean size = intent.getBooleanExtra(ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE_PACKET, false);

            if (size) {
                // 是第一个数据包,此处初始化进度条
                // todo:此处初始化进度条，获得进度条总长度
                refCoeffDataProgressTotalSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0);
                refCoeffDataProgressCurrentProgress = 0;
                Log.d(TAG, "扫描页-RefCoeffDataProgressReceiver中EXTRA_REF_CAL_COEFF_SIZE_PACKET为true，当前为第一个数据包。");
                Log.d(TAG, "扫描页-RefCoeffDataProgressReceiver中接收到数据包总大小为：" + refCoeffDataProgressTotalSize);
            } else {
                // 不是第一个数据包，ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE代表当前数据包大小，更新进度条
                // todo:此处更新进度
//                barProgressDialog.setProgress(barProgressDialog.getProgress() + intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0));
                int currentSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0);
                refCoeffDataProgressCurrentProgress += currentSize;
                String receivingProgress = getString(R.string.calibration_coefficient_receiving, String.valueOf(refCoeffDataProgressCurrentProgress), String.valueOf(refCoeffDataProgressTotalSize));
                tv_load_calibration.setText(receivingProgress);
                Log.d(TAG, "扫描页-RefCoeffDataProgressReceiver中EXTRA_REF_CAL_COEFF_SIZE_PACKET为false，当前数据包大小为：" + currentSize);
            }
            if (refCoeffDataProgressCurrentProgress == refCoeffDataProgressTotalSize) {
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);
                tv_load_calibration.setText(getString(R.string.calibration_coefficient_received));
            }
        }
    }

    // 此广播接收器可以查看校准矩阵的接收进度，ACTION_REQ_CAL_MATRIX(ISCNIRScanSDK.SetCurrentTime()must be called)
    public class CalMatrixDataProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int intExtra = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
            Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver-onReceive called，获取校准矩阵的接收进度。矩阵大小：" + intExtra);
            // 标记是否是第一个数据包，便于初始化进度条。
            boolean size = intent.getBooleanExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE_PACKET, false);
            if (size) {
                // todo:此处初始化进度条，获得进度条总长度
                refCoeffDataProgressTotalSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                refCoeffDataProgressCurrentProgress = 0;
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE_PACKET为true，当前为第一个数据包。");
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中接收到数据包总大小为：" + refCoeffDataProgressTotalSize);
            } else {
                // todo:此处更新进度
                int currentSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                refCoeffDataProgressCurrentProgress += currentSize;
                String receivingProgress = getString(R.string.calibration_matrix_receiving, String.valueOf(refCoeffDataProgressCurrentProgress), String.valueOf(refCoeffDataProgressTotalSize));
                tv_load_calibration.setText(receivingProgress);
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE为false，当前数据包大小为：" + currentSize);
            }

            if (refCoeffDataProgressCurrentProgress == refCoeffDataProgressTotalSize) {
                tv_load_calibration.setText(getString(R.string.calibration_matrix_received));
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);
                // todo:当接收完毕后，调用 ISCNIRScanSDK.GetActiveConfig();，SDK将发送broadcast GET_ACTIVE_CONF，获取活动配置的索引。
                ISCNIRScanSDK.GetActiveConfig();
            }

        }
    }


    // 此广播接收器可以获取校准系数和校准矩阵具体的数值，REF_CONF_DATA
    public class RefDataReadyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-RefDataReadyReceiver called,获取校准系数和校准矩阵具体的数值.");
            refCoeff = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_REF_COEF_DATA);
            refMatrix = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_REF_MATRIX_DATA);
            ArrayList<ISCNIRScanSDK.ReferenceCalibration> refCal = new ArrayList<>();
            refCal.add(new ISCNIRScanSDK.ReferenceCalibration(refCoeff, refMatrix));
            ISCNIRScanSDK.ReferenceCalibration.writeRefCalFile(mContext, refCal);

            // 将获取到的校准系数，存储到MainActivity中的静态内部类的成员变量中。
            MainActivity.StoreCalibration.device = preferredDevice;  // 用户选中的设备的mac
            MainActivity.StoreCalibration.storrefCoeff = refCoeff;
            MainActivity.StoreCalibration.storerefMatrix = refMatrix;
            Log.d(TAG, "扫描页-RefDataReadyReceiver完成-refCoeff:" + Arrays.toString(refCoeff) + "\n-refMatrix:" +
                    Arrays.toString(refMatrix));
        }
    }

    // 用于获取设备的活动扫描配置的索引。当设备下载完校准矩阵数据后，会发送 GET_ACTIVE_CONF 广播
    private class GetActiveScanConfReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取活动配置索引
            ActiveConfigIndex = Objects.requireNonNull(intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_ACTIVE_CONF))[0];
            if (!ScanConfigList.isEmpty()) {
                // todo:实现本地读取扫描配置
//                GetActiveConfigOnResume();
            } else {
                // 如果 ScanConfigList 为空，说明尚未获取设备中的扫描配置列表。
                // 调用 ISCNIRScanSDK.GetScanConfig() 函数来请求设备发送扫描配置列表。
                ISCNIRScanSDK.GetScanConfig();
            }

        }
    }

    // todo:完成GetActiveConfigOnResume的逻辑
    private void GetActiveConfigOnResume() {

    }

    // 获取设备的扫描配置文件数量
    public class ScanConfSizeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            storedConfSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_CONF_SIZE, 0);
            Log.d(TAG, "扫描页-ScanConfSizeReceiver被调用，storedConfSize：" + storedConfSize);
        }
    }

    // 获取设备的扫描配置文件
    public class ScanConfReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            receivedConfSize++;
            ScanConfig_Byte_List.add(intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DATA));
            ScanConfigList.add(ISCNIRScanSDK.scanConf);
            // 配置文件接收完成
            if (receivedConfSize == storedConfSize) {
                // 从ScanConfigList和ScanConfig_Byte_List中根据活动配置ActiveConfigIndex取出配置信息
                for (int i = 0; i < ScanConfigList.size(); i++) {
                    int ScanConfigIndexToByte = ScanConfigList.get(i).getScanConfigIndex();
                    if (ActiveConfigIndex == ScanConfigIndexToByte) {
                        activeConf = ScanConfigList.get(i);
                        ActiveConfigByte = ScanConfig_Byte_List.get(i);
                    }
                }
            }
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "扫描页-onServiceConnected called,服务已连接！");
            // 获得ISCNIRScanSDK服务对象
            mNanoBLEService = ((ISCNIRScanSDK.LocalBinder) iBinder).getService();
            //初始化 bluetooth, 如果 BLE 不可用, 则 finish
            if (!mNanoBLEService.initialize()) {
                Log.e(TAG, "扫描页-BLE 不可用，活动结束！");
                finish();
            }
            // 蓝牙管理
            BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            // 蓝牙适配器
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // 蓝牙Scanner
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothLeScanner == null) {
                Log.e(TAG, "扫描页-BluetoothLeScanner 不可用，活动结束！");
                finish();
                Toast.makeText(ScanViewActivity.this, "请确保蓝牙已经打开！", Toast.LENGTH_SHORT).show();
                return;
            }
            mHandler = new Handler();
            // 如果存储的设备MAC不为空
            String deviceMac = getStringPref(ScanViewActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (deviceMac != null) {
                // 将成员变量preferredDevice进行赋值。
                preferredDevice = deviceMac;
                Log.d(TAG, "扫描页-获取到存储的设备MAC！mac:" + deviceMac);
                // 开始扫描附加是否有选择的
                scanPreferredLeDevice(true);
            } else {  // 如果存储的设备MAC为空
                Log.e(TAG, "扫描页-存储的设备MAC为空!");
                scanLeDevice(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 当服务断连
            mNanoBLEService = null;
        }
    };


    @SuppressLint("MissingPermission")
    private final ScanCallback mPreferredLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "扫描页-mPreferredLeScanCallback的onScanResult被调用! result:" + result.toString());
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String preferredNano = getStringPref(ScanViewActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (preferredNano != null && name != null) {
                // 设备名称包含设备前缀(DEVICE_NAME)，且设备MAC地址等于选中设备的MAC地址
                if (name.contains(DEVICE_NAME) && device.getAddress().equals(preferredNano)) {
                    // 连接当前的设备
                    Log.d(TAG, "扫描页-mPreferredLeScanCallback的onScanResult 成功获取preferredNano和name"
                            + preferredNano + "，name:" + name + "。已连接设备！connected = true");
                    mNanoBLEService.connect(device.getAddress());
                    connected = true;
                    scanPreferredLeDevice(false);

                    tv_load_calibration.setText(getString(R.string.connected_to_the_device));
                }
            } else {
                Log.e(TAG, "扫描页面-mPreferredLeScanCallback的onScanResult中，" +
                        "preferredNano或name为null! preferredNano:" + preferredNano + "，name:" + name);
            }
        }
    };


    @SuppressLint("MissingPermission")
    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "扫描页-mLeScanCallback的onScanResult被调用! result:" + result.toString());
            BluetoothDevice device = result.getDevice();
            String preferredNano = getStringPref(ScanViewActivity.this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if (preferredNano != null) {
                if (preferredNano.equals(device.getAddress())) {
                    Log.d(TAG, "扫描页-mLeScanCallback的onScanResult ，preferredNano和device.getAddress()一致（mac一致）！"
                            + preferredNano + "，device.getAddress:" + device.getAddress() + "。正式开始连接设备！");
                    mNanoBLEService.connect(preferredNano);
                    connected = true;
                    scanLeDevice(false);
                }
            }
        }
    };

    // 改变灯状态
    private void ChangeLampState() {
        if (warmUp) {
            ISCNIRScanSDK.ControlLamp(ISCNIRScanSDK.LampState.AUTO);
            warmUp = false;
        }
//        if (Current_Scan_Method == ScanMethod.Manual && toggle_button_manual_scan_mode.isChecked())//Manual->Normal,Quickset,Maintain
//        {
//            if (toggle_button_manual_lamp.getText().toString().toUpperCase().equals("ON")) {
//                toggle_button_manual_lamp.setChecked(false);//close lamp
//            }
//            ISCNIRScanSDK.ControlLamp(ISCNIRScanSDK.LampState.AUTO);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "扫描页-onPause called");
        // todo:按照SDK中的建议，如果onPause不是为了跳转其他界面，则应该finish()当前Activity。

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "扫描页-onDestroy called");
        // 改变光源状态，避免软件退出后光源常亮
        ChangeLampState();
        // todo:解绑服务、取消广播接收器的注册
        unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(RefCoeffDataProgressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(NotifyCompleteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(CalMatrixDataProgressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(RefDataReadyReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnSetLampReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GetActiveScanConfReceiver);
    }
}
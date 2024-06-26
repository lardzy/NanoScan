package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.getBooleanPref;
import static com.ISCSDK.ISCNIRScanSDK.getStringPref;
import static com.ISCSDK.ISCNIRScanSDK.storeBooleanPref;
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
import java.util.regex.Pattern;

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
    private final BroadcastReceiver ScanConfSizeReceiver = new ScanConfSizeReceiver();
    private final BroadcastReceiver ScanConfReceiver = new ScanConfReceiver();
    private final BroadcastReceiver SpectrumCalCoefficientsReadyReceiver = new SpectrumCalCoefficientsReadyReceiver();
    private final BroadcastReceiver DeviceInfoReceiver = new DeviceInfoReceiver();
    private final BroadcastReceiver ReturnMFGNumReceiver = new ReturnMFGNumReceiver();
    private final BroadcastReceiver GetUUIDReceiver = new GetUUIDReceiver();
    private final BroadcastReceiver ReturnReadActivateStatusReceiver = new ReturnReadActivateStatusReceiver();

    private final IntentFilter requestCalCoeffFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_COEFF);
    private final IntentFilter refReadyFilter = new IntentFilter(ISCNIRScanSDK.REF_CONF_DATA);
    private final IntentFilter notifyCompleteFilter = new IntentFilter(ISCNIRScanSDK.ACTION_NOTIFY_DONE);
    private final IntentFilter requestCalMatrixFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_MATRIX);
    private final IntentFilter scanConfFilter = new IntentFilter(ISCNIRScanSDK.SCAN_CONF_DATA);
    private final IntentFilter SpectrumCalCoefficientsReadyFilter = new IntentFilter(ISCNIRScanSDK.SPEC_CONF_DATA);
    private final IntentFilter ReturnMFGNumFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_MFGNUM);
    private final IntentFilter ReturnReadActivateStatusFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_READ_ACTIVATE_STATE);

    // endregion
    private boolean warmUp = false;
    private boolean mainFlag = false;
    private LampInfo lampInfo = LampInfo.ManualLamp;
    private ArrayList<ISCNIRScanSDK.ScanConfiguration> scanConfigList = new ArrayList<>();
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
    private int refCoeffDataProgressTotalSize = 0;
    private int refCoeffDataProgressCurrentProgress = 0;
    private int calMatrixDataProgressTotalSize = 0;
    private int calMatrixDataProgressCurrentProgress = 0;
    // 记录光谱校准系数
    private byte[] spectrumCalCoefficients = new byte[144];
    //允许 AddScanConfigViewActivity 获得光谱校准系数，以计算 max pattern
    public static byte[] passSpectrumCalCoefficients = new byte[144];
    // 检查是否已接收到光谱校准系数
    private Boolean downloadSpecFlag = false;
    // endregion
    // 设备是否已经连接上
    private boolean connected;
    // region 设备配置文件
    // 存储设备配置文件数量
    private int storedConfSize;
    // 记录已经接收到的配置文件内容数量
    private int receivedConfSize = 0;
    // 记录扫描配置字节列表
    private ArrayList<byte[]> scanConfigByteList = new ArrayList<>();
    // 从设备获取的活动配置索引
    private int activeConfigIndex;
    // 设备当前活动的扫描配置
    private ISCNIRScanSDK.ScanConfiguration activeConf;
    // 存储当前活动配置的字节数组
    private byte[] activeConfigByte;
    // endregion
    // region GetDeviceStatusReceiver使用的变量、常量。
    private String battery = "";
    private String totalLampTime = "";
    private byte[] devbyte;
    private byte[] errbyte;
    private float temprature;
    private float humidity;
    // endregion
    // region DeviceInfoReceiver使用的变量、常量。
    private String model_name = "";
    private String serial_num = "";
    private String HWrev = "";
    private String Tivarev = "";
    private String Specrev = "";
    // Tiva版本是否是标准波长、扩展波长、扩展plus波长
    public static Boolean isExtendVer = false;
    public static Boolean isExtendVer_PLUS = false;
    //! Control FW level to implement function
    public static ISCNIRScanSDK.FW_LEVEL_STANDARD fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_0;
    public static ISCNIRScanSDK.FW_LEVEL_EXT fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_1;
    public static ISCNIRScanSDK.FW_LEVEL_EXT_PLUS fw_level_ext_plus = ISCNIRScanSDK.FW_LEVEL_EXT_PLUS.LEVEL_EXT_PLUS_1;
    // endregion
    private float minWavelength = 900;
    private float maxWavelength = 1700;
    private int MINWAV = 900;
    private int MAXWAV = 1700;
    private float minAbsorbance = 0;
    private float maxAbsorbance = 2;
    private float minReflectance = -2;
    private float maxReflectance = 2;
    private float minIntensity = -7000;
    private float maxIntensity = 7000;
    private float minReference = -7000;
    private float maxReference = 7000;
    private int numSections = 0;

    private byte[] MFG_NUM;
    private String HW_Model = "";
    private String uuid = "";
    private boolean isOldTiva = false;

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
        //region 注册所有 broadcast receivers
        Log.d(TAG, "扫描页-开始注册广播。");
        LocalBroadcastManager.getInstance(this).registerReceiver(GetDeviceStatusReceiver, new IntentFilter(ISCNIRScanSDK.ACTION_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(RefCoeffDataProgressReceiver, requestCalCoeffFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(NotifyCompleteReceiver, notifyCompleteFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(CalMatrixDataProgressReceiver, requestCalMatrixFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(RefDataReadyReceiver, refReadyFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnSetLampReceiver, new IntentFilter(ISCNIRScanSDK.SET_LAMPSTATE_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(GetActiveScanConfReceiver, new IntentFilter(ISCNIRScanSDK.SEND_ACTIVE_CONF));
        LocalBroadcastManager.getInstance(this).registerReceiver(ScanConfSizeReceiver, new IntentFilter(ISCNIRScanSDK.SCAN_CONF_SIZE));
        LocalBroadcastManager.getInstance(this).registerReceiver(ScanConfReceiver, scanConfFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(SpectrumCalCoefficientsReadyReceiver, SpectrumCalCoefficientsReadyFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(DeviceInfoReceiver, new IntentFilter(ISCNIRScanSDK.ACTION_INFO));
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnMFGNumReceiver, ReturnMFGNumFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(GetUUIDReceiver, new IntentFilter(ISCNIRScanSDK.SEND_DEVICE_UUID));
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnReadActivateStatusReceiver, ReturnReadActivateStatusFilter);
        // endregion
    }

    private void initialComponent() {
        view_back = findViewById(R.id.view_back);
        start_scan_button = findViewById(R.id.start_scan_button);
        pb_load_calibration = findViewById(R.id.pb_load_calibration);
        tv_load_calibration = findViewById(R.id.tv_load_calibration);
        rv_function_list = findViewById(R.id.rv_function_list);

        view_back.setOnClickListener(this);

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
        mainFlag = getIntent().getBooleanExtra("mainFlag", false);
        Log.d(TAG, "扫描页-获取到deviceItem：" + deviceItem + "\n" + "获取到warmUp：" + warmUp);
        if (deviceItem != null) {
            Log.d(TAG, "扫描页-获取到传入的设备对象，准备存储DeviceMac、DeviceName到SDK：" + deviceItem);
            // 使用SDK中的方法，存储选中的设备信息，包括设备mac和名称
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDevice, deviceItem.getDeviceMac());
            storeStringPref(this, ISCNIRScanSDK.SharedPreferencesKeys.preferredDeviceModel, deviceItem.getDeviceName());
        } else {
            Log.e(TAG, "扫描页-获取到传入的设备对象deviceItem为NULL！");
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
                    tv_load_calibration.setAnimation(fadeIn);
                    tv_load_calibration.startAnimation(fadeIn);
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
            totalLampTime = GetLampTimeString(lamptime);
            devbyte = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DEV_STATUS_BYTE);
            Log.e(TAG, "battery:" + battery + "TotalLampTime:" + totalLampTime + "devbyte:" + Arrays.toString(devbyte));
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
                calMatrixDataProgressTotalSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                calMatrixDataProgressCurrentProgress = 0;
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE_PACKET为true，当前为第一个数据包。");
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中接收到数据包总大小为：" + calMatrixDataProgressTotalSize);
            } else {
                // todo:此处更新进度
                int currentSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                calMatrixDataProgressCurrentProgress += currentSize;
                String receivingProgress = getString(R.string.calibration_matrix_receiving, String.valueOf(calMatrixDataProgressCurrentProgress), String.valueOf(calMatrixDataProgressTotalSize));
                tv_load_calibration.setText(receivingProgress);
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE为false，当前数据包大小为：" + currentSize);
            }

            if (calMatrixDataProgressCurrentProgress == calMatrixDataProgressTotalSize) {
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
            activeConfigIndex = Objects.requireNonNull(intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_ACTIVE_CONF))[0];
            Log.d(TAG, "扫描页-GetActiveScanConfReceiver获取到活动扫描配置索引：activeConfigIndex：" + activeConfigIndex);
            if (!scanConfigList.isEmpty()) {
                // todo:实现本地读取扫描配置
                GetActiveConfigOnResume();
                Log.d(TAG, "扫描页-GetActiveScanConfReceiver中scanConfigList不为空，从本地回去扫描配置。scanConfigList.size = " + scanConfigList.size());
            } else {
                // 如果 ScanConfigList 为空，说明尚未获取设备中的扫描配置列表。
                // 调用 ISCNIRScanSDK.GetScanConfig() 函数来请求设备发送扫描配置列表。
                ISCNIRScanSDK.GetScanConfig();
                Log.d(TAG, "扫描页-GetActiveScanConfReceiver中scanConfigList长度为0，" +
                        "调用APIISCNIRScanSDK.GetScanConfig();");
            }

        }
    }

    // todo:完成GetActiveConfigOnResume的逻辑
    private void GetActiveConfigOnResume() {
        for (int i = 0; i < scanConfigList.size(); i++) {
            int scanConfigIndexToByte = scanConfigList.get(i).getScanConfigIndex();
            if (activeConfigIndex == scanConfigIndexToByte) {
                activeConf = scanConfigList.get(i);
                activeConfigByte = scanConfigByteList.get(i);

                tv_load_calibration.setText(getString(R.string.active_scan_config_read_from_local));
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);

                Log.d(TAG, "扫描页-GetActiveConfigOnResume中activeConfigByte:" + Arrays.toString(activeConfigByte) + "\n" +
                        "activeConfigIndex:" + activeConfigIndex + "\nscanConfigIndexToByte:" + scanConfigIndexToByte);
            }
        }
    }

    // 获取设备内部存储的扫描配置文件数量
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
            scanConfigByteList.add(intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DATA));
            scanConfigList.add(ISCNIRScanSDK.scanConf);
            tv_load_calibration.setText(getString(R.string.active_scan_config_receiving, String.valueOf(receivedConfSize), String.valueOf(storedConfSize)));
            Log.d(TAG, "扫描页-ScanConfReceiver获取到扫描配置：receivedConfSize:" + receivedConfSize + "\n" +
                    "scanConfigByteList.size:" + scanConfigByteList.size() + "\nscanConfigList.size:" + scanConfigList.size() + "\n" +
                    "storedConfSize:" + storedConfSize);
            // 配置文件接收完成
            if (receivedConfSize == storedConfSize) {
                // 从ScanConfigList和ScanConfig_Byte_List中根据活动配置ActiveConfigIndex取出配置信息
                tv_load_calibration.setText(getString(R.string.active_scan_config_received));
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);

                for (int i = 0; i < scanConfigList.size(); i++) {
                    Log.d(TAG, "扫描页-ScanConfReceiver扫描配置获取完成。i:" + i);
                    int ScanConfigIndexToByte = scanConfigList.get(i).getScanConfigIndex();
                    // 应用已经激活的配置
                    if (activeConfigIndex == ScanConfigIndexToByte) {
                        activeConf = scanConfigList.get(i);
                        activeConfigByte = scanConfigByteList.get(i);
                    }
                }
                if (!downloadSpecFlag) {
                    // 请求获取光谱校准系数
                    ISCNIRScanSDK.GetSpectrumCoef();
                    downloadSpecFlag = true;
                }
            }
        }
    }

    // 获取光谱校准系数
    public class SpectrumCalCoefficientsReadyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            spectrumCalCoefficients = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_SPEC_COEF_DATA);
            passSpectrumCalCoefficients = spectrumCalCoefficients;
            Log.d(TAG, "扫描页-SpectrumCalCoefficientsReadyReceiver called。光谱校准系数 spectrumCalCoefficients ：" + Arrays.toString(passSpectrumCalCoefficients));
            // 请求获取设备信息
            ISCNIRScanSDK.GetDeviceInfo();
        }
    }

    // 获取设备信息
    public class DeviceInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            model_name = intent.getStringExtra(ISCNIRScanSDK.EXTRA_MODEL_NUM);
            serial_num = intent.getStringExtra(ISCNIRScanSDK.EXTRA_SERIAL_NUM);
            HWrev = intent.getStringExtra(ISCNIRScanSDK.EXTRA_HW_REV);
            Tivarev = intent.getStringExtra(ISCNIRScanSDK.EXTRA_TIVA_REV);
            Specrev = intent.getStringExtra(ISCNIRScanSDK.EXTRA_SPECTRUM_REV);
            Log.d(TAG, String.format("扫描页-DeviceInfoReceiver，model_name：%1$s，serial_num：%2$s, HWrev：%3$s，Tivarev：%4$s，Specrev：%5$s", model_name,
                    serial_num, HWrev, Tivarev, Specrev));
            if (Tivarev.charAt(0) == '5') {
                isExtendVer_PLUS = true;
                isExtendVer = false;
            } else if (Tivarev.charAt(0) == '3' && (HWrev.charAt(0) == 'E' || HWrev.charAt(0) == 'O')) {
                isExtendVer_PLUS = false;
                isExtendVer = true;
            } else {
                isExtendVer_PLUS = false;
                isExtendVer = false;
            }
            Log.d(TAG, "扫描页-DeviceInfoReceiver,isExtendVer_PLUS:" + isExtendVer_PLUS + ",isExtendVer:" + isExtendVer);
            if ((isExtendVer || isExtendVer_PLUS) && serial_num.length() > 8)
                serial_num = serial_num.substring(0, 8);
            else if (!isExtendVer_PLUS && !isExtendVer && serial_num.length() > 7)
                serial_num = serial_num.substring(0, 7);
            Log.d(TAG, "扫描页-DeviceInfoReceiver，获取到serial_num前7位：" + serial_num);
            if (HWrev.charAt(0) == 'N') {
                notConnectedDialog();
                Toast.makeText(mContext, "设备不支持！", Toast.LENGTH_LONG).show();
            } else {
                if (isExtendVer) {
                    ISCNIRScanSDK.TIVAFW_EXT = GetFWLevelEXT(Tivarev);
                } else if (isExtendVer_PLUS) {
                    ISCNIRScanSDK.TIVAFW_EXT_PLUS = getFWLevelEXTPLUS(Tivarev);
                } else {
                    ISCNIRScanSDK.TIVAFW_STANDARD = getFWLevelStandard(Tivarev);
                }
                initParameter();
                if (!isExtendVer_PLUS && !isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_0) == 0) {
                    notConnectedDialog();
                    Toast.makeText(mContext, "设备固件版本过低！最低 V2.4.4.，当前V" + Tivarev + ".。", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "扫描页-DeviceInfoReceiver，设备固件版本过低！当前V" + Tivarev);
                } else {
                    // 当设备TIVA版本≥2.1.0.67，则继续请求获取设备制造序列号
                    Log.d(TAG, "扫描页-DeviceInfoReceiver，设备TIVA版本≥2.1.0.67，继续后续步骤。");
                    ISCNIRScanSDK.GetMFGNumber();
                }
            }
        }
    }

    // 获取设备制造序列号
    public class ReturnMFGNumReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MFG_NUM = intent.getByteArrayExtra(ISCNIRScanSDK.MFGNUM_DATA);
            Log.d(TAG, "扫描页-ReturnMFGNumReceiver called，MFG_NUM：" + Arrays.toString(MFG_NUM));
            // 当Tiva为 2.5.x 时，调用GetHWModel。
            if ((!isExtendVer_PLUS && !isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4) >= 0) || (isExtendVer && fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_3) >= 0)) {
                Log.d(TAG, "扫描页-ReturnMFGNumReceiver，设备为标准光谱，fw_level_standardcompareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4)>=0，具体为:" + fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4));
                ISCNIRScanSDK.GetHWModel();
            } else {
                Log.d(TAG, "扫描页-ReturnMFGNumReceiver，设备为标准光谱，fw_level_standardcompareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4)<0，具体为:" + fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4));

                ISCNIRScanSDK.GetUUID();
            }
        }
    }

    public class GetUUIDReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] buf = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DEVICE_UUID);
            StringBuilder uuidBuilder = new StringBuilder();
            for (int i = 0; i < Objects.requireNonNull(buf).length; i++) {
                uuidBuilder.append(Integer.toHexString(0xff & buf[i]));
                if (i != buf.length - 1) {
                    uuidBuilder.append(":");
                }
            }
            uuid = uuidBuilder.toString();
            Log.d(TAG, "扫描页-GetUUIDReceiver，获取到UUID:" + uuid);
            checkIsOldTIVA();
            if (!isOldTiva) { // 设备固件非旧版本Tiva≥2.4.4
                // 调用以获取设备激活状态
                ISCNIRScanSDK.ReadActivateState();
            }
            // todo: 未来有必要的前提下，实现旧版本兼容, 现有设备 Tivarev:2.4.7、fw_level_standard：LEVEL_3。

            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DeviceInfoReceiver);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetUUIDReceiver);
        }
    }

    // 获取设备是否激活
    public class ReturnReadActivateStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 只有从 DeviceDetailsActivity 跳转过来时，才执行下面的代码。
            if (mainFlag) {
                // 设置活动扫描配置，防止设备使用 WPF 或 WinForm 本地配置
                ISCNIRScanSDK.SetActiveConfig();
                Log.d(TAG, "扫描页-ReturnReadActivateStatusReceiver，mainFlag为true，call ISCNIRScanSDK.SetActiveConfig()");
                mainFlag = false;
            }
            byte[] state = intent.getByteArrayExtra(ISCNIRScanSDK.RETURN_READ_ACTIVATE_STATE);
            if (Objects.requireNonNull(state)[0] == 1) {
                new Handler().postDelayed(() -> {
                    setDeviceButtonStatus();
                    // todo:显示设备已激活
                    tv_load_calibration.setText(getText(R.string.device_activated));
                    tv_load_calibration.setAnimation(fadeIn);
                    tv_load_calibration.startAnimation(fadeIn);
                }, 300);
                storeStringPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.Activacatestatus, "Activated.");
            } else {
                Toast.makeText(mContext, "设备激活失败！", Toast.LENGTH_LONG).show();
                finish();
            }
//            else {
//                // todo:检查本地是否有存储许可证
//                String licenseKey = getStringPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.licensekey, null);
//                if (licenseKey != null && !licenseKey.isEmpty()) {
//
//                }
//            }

        }
    }

    // 设定设备物理按钮状态
    private void setDeviceButtonStatus() {
        // 校验设备波长类型、fw_level
        if (isExtendVer_PLUS || isExtendVer || fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_1) > 0) {
            boolean isLockButton = getBooleanPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.LockButton, false);
            //User open lock button on Configure page
            if (isLockButton) {
                ISCNIRScanSDK.ControlPhysicalButton(ISCNIRScanSDK.PhysicalButton.Lock);
            } else {
                ISCNIRScanSDK.ControlPhysicalButton(ISCNIRScanSDK.PhysicalButton.Unlock);
            }
        }
    }

    // 检查是否是低Tiva版本
    private void checkIsOldTIVA() {
        String[] TivaArray = Tivarev.split(Pattern.quote("."));

        if (!isExtendVer_PLUS && !isExtendVer && (Integer.parseInt(TivaArray[1]) < 4 || Integer.parseInt(TivaArray[1]) < 4))//Tiva <2.4.4(the newest version)
        {
            isOldTiva = true;
            // todo:提示用户固件版本过低
            notConnectedDialog();
            Toast.makeText(this, "设备固件版本过低！最低 V2.4.4.，当前V" + Tivarev + ".。", Toast.LENGTH_LONG).show();
        } else {
            isOldTiva = false;
        }

    }

    //    region 根据Tiva版本获取 FW level
    // 根据Tiva版本获取扩展波长FW level
    private ISCNIRScanSDK.FW_LEVEL_EXT GetFWLevelEXT(String Tivarev) {
        String[] TivaArray = Tivarev.split(Pattern.quote("."));
        String[] split_hw = HWrev.split("\\.");
        fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_1;
        if (Integer.parseInt(TivaArray[1]) >= 5 && split_hw[0].equals("O")) {
                 /*New Applications:
                  1. Use new command to read ADC value and timestamp
                 */
            fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_4;//>=3.5.X and main board = "O"
        } else if (Integer.parseInt(TivaArray[1]) >= 5) {
                 /*New Applications:
                  1. Support get pga
                 */
            fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_3;//>=3.5.X
        } else if (Integer.parseInt(TivaArray[1]) >= 3 && split_hw[0].equals("O")) {
                 /*New Applications:
                  1. Support read ADC value
                 */
            fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_2;//>=3.3.0 and main board = "O"
        } else if (Integer.parseInt(TivaArray[1]) >= 3) {
                /*New Applications:
                  1. Add Lock Button
                 */
            fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_1;//>=3.3.0
        } else if (Integer.parseInt(TivaArray[1]) == 2 && Integer.parseInt(TivaArray[2]) == 1)
            fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_1;//==3.2.1

        return fw_level_ext;
    }

    // 根据Tiva版本获取标准波长plus FW level
    private ISCNIRScanSDK.FW_LEVEL_EXT_PLUS getFWLevelEXTPLUS(String Tivarev) {
//        String[] TivaArray = Tivarev.split(Pattern.quote("."));
//        String split_hw[] = HWrev.split("\\.");
        fw_level_ext_plus = ISCNIRScanSDK.FW_LEVEL_EXT_PLUS.LEVEL_EXT_PLUS_1;
        return fw_level_ext_plus;
    }

    // 根据Tiva版本获取标准波长 FW level
    private ISCNIRScanSDK.FW_LEVEL_STANDARD getFWLevelStandard(String Tivarev) {
        Log.d(TAG, "扫描页-GetFWLevelStandard called. Tivarev:" + Tivarev);
        String[] TivaArray = Tivarev.split(Pattern.quote("."));
        String[] split_hw = HWrev.split("\\.");
        fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_0;
        if (Integer.parseInt(TivaArray[1]) >= 5 && split_hw[0].equals("F")) {
                /*New Applications:
                  1. Use new command to read ADC value and timestamp
                 */
            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_5;//>=2.5.X and main board ="F"
        } else if (Integer.parseInt(TivaArray[1]) >= 5) {
                /*New Applications:
                  1. Support get pga
                 */
            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4;//>=2.5.X
        } else if (Integer.parseInt(TivaArray[1]) >= 4 && Integer.parseInt(TivaArray[2]) >= 3 && split_hw[0].equals("F")) {
                /*New Applications:
                  1. Support read ADC value
                 */
            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_3;//>=2.4.4 and main board ="F"
        } else if ((Integer.parseInt(TivaArray[1]) >= 4 && Integer.parseInt(TivaArray[2]) >= 3) || Integer.parseInt(TivaArray[1]) >= 5) {
                /*New Applications:
                  1. Add Lock Button
                 */
            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_2;//>=2.4.4
        } else if ((TivaArray.length == 3 && Integer.parseInt(TivaArray[1]) >= 1) || (TivaArray.length == 4 && Integer.parseInt(TivaArray[3]) >= 67))//>=2.1.0.67
        {
            //New Applications:
            // 1. Support activate state

            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_1;
        } else {
            fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_0;
        }
        Log.d(TAG, "扫描页-GetFWLevelStandard，fw_level_standard：" + fw_level_standard);
        return fw_level_standard;
    }

    // 根据设备类型，初始化波长范围
    private void initParameter() {
        Log.d(TAG, "扫描页-InitParameter called.");

        if (isExtendVer) {
            minWavelength = 1350;
            maxWavelength = 2150;
            MINWAV = 1350;
            MAXWAV = 2150;
        } else if (isExtendVer_PLUS) {
            minWavelength = 1600;
            maxWavelength = 2400;
            MINWAV = 1600;
            MAXWAV = 2400;
        } else {
            minWavelength = 900;
            maxWavelength = 1700;
            MINWAV = 900;
            MAXWAV = 1700;
        }
//        et_quickset_spec_start.setText(Integer.toString(MINWAV));
//        et_quickset_spec_end.setText(Integer.toString(MAXWAV));
//        quickset_init_start_nm = (Integer.parseInt(et_quickset_spec_start.getText().toString()));
//        quickset_init_end_nm = (Integer.parseInt(et_quickset_spec_end.getText().toString()));
        //不支持锁定按钮
        if (!isExtendVer_PLUS && !isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_1) <= 0)
            storeBooleanPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.LockButton, false);
    }

    // endregion

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "扫描页-onServiceConnected called,服务已连接！");
            // 获得ISCNIRScanSDK服务对象
            mNanoBLEService = ((ISCNIRScanSDK.LocalBinder) iBinder).getService();
            //初始化 bluetooth, 如果 BLE 不可用, 则 finish
            if (!mNanoBLEService.initialize()) {
                Log.e(TAG, "扫描页-BLE 不可用，Activity结束！");
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
    private void changeLampState() {
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
        changeLampState();
        // todo:解绑服务、取消广播接收器的注册
        unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(RefCoeffDataProgressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(NotifyCompleteReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(CalMatrixDataProgressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(RefDataReadyReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnSetLampReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GetActiveScanConfReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ScanConfSizeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ScanConfReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SpectrumCalCoefficientsReadyReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DeviceInfoReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnMFGNumReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(GetUUIDReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnReadActivateStatusReceiver);
    }
}
package com.gttcgf.nanoscan;

import static com.ISCSDK.ISCNIRScanSDK.Interpret_intensity;
import static com.ISCSDK.ISCNIRScanSDK.Interpret_length;
import static com.ISCSDK.ISCNIRScanSDK.Interpret_uncalibratedIntensity;
import static com.ISCSDK.ISCNIRScanSDK.Interpret_wavelength;
import static com.ISCSDK.ISCNIRScanSDK.Reference_Info;
import static com.ISCSDK.ISCNIRScanSDK.Scan_Config_Info;
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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ISCSDK.ISCNIRScanSDK;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gttcgf.nanoscan.tools.PasswordUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class ScanViewActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ScanViewActivity";
    //允许 AddScanConfigViewActivity 获得光谱校准系数，以计算 max pattern
    public static byte[] passSpectrumCalCoefficients = new byte[144];
    // Tiva版本是否是标准波长、扩展波长、扩展plus波长
    public static Boolean isExtendVer = false;
    public static Boolean isExtendVer_PLUS = false;
    //! Control FW level to implement function
    public static ISCNIRScanSDK.FW_LEVEL_STANDARD fw_level_standard = ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_0;
    public static ISCNIRScanSDK.FW_LEVEL_EXT fw_level_ext = ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_1;
    public static ISCNIRScanSDK.FW_LEVEL_EXT_PLUS fw_level_ext_plus = ISCNIRScanSDK.FW_LEVEL_EXT_PLUS.LEVEL_EXT_PLUS_1;
    private static String DEVICE_NAME = "NIR";
    // region broadcast 接收器、过滤器
    private final BroadcastReceiver StatusReceiver = new StatusReceiver();
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
    private final BroadcastReceiver ReturnLampRampUpADCReceiver = new ReturnLampRampUpADCReceiver();
    private final BroadcastReceiver ReturnLampADCAverageReceiver = new ReturnLampADCAverageReceiver();
    private final BroadcastReceiver ScanDataReadyReceiver = new ScanDataReadyReceiver();

    private final IntentFilter requestCalCoeffFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_COEFF);
    private final IntentFilter refReadyFilter = new IntentFilter(ISCNIRScanSDK.REF_CONF_DATA);
    private final IntentFilter notifyCompleteFilter = new IntentFilter(ISCNIRScanSDK.ACTION_NOTIFY_DONE);
    private final IntentFilter requestCalMatrixFilter = new IntentFilter(ISCNIRScanSDK.ACTION_REQ_CAL_MATRIX);
    private final IntentFilter scanConfFilter = new IntentFilter(ISCNIRScanSDK.SCAN_CONF_DATA);
    private final IntentFilter SpectrumCalCoefficientsReadyFilter = new IntentFilter(ISCNIRScanSDK.SPEC_CONF_DATA);
    private final IntentFilter ReturnMFGNumFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_MFGNUM);
    private final IntentFilter ReturnReadActivateStatusFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_READ_ACTIVATE_STATE);
    private final IntentFilter ReturnLampRampUpFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_LAMP_RAMPUP_ADC);
    private final IntentFilter ReturnLampADCAverageFilter = new IntentFilter(ISCNIRScanSDK.ACTION_RETURN_LAMP_AVERAGE_ADC);
    private final IntentFilter scanDataReadyFilter = new IntentFilter(ISCNIRScanSDK.SCAN_DATA);
    private Context mContext;
    // endregion
    private boolean warmUp = false;
    private boolean mainFlag = false;
    private LampInfo lampInfo = LampInfo.ManualLamp;
    private ArrayList<ISCNIRScanSDK.ScanConfiguration> scanConfigList = new ArrayList<>();
    // region UI组件
    private ImageButton imageButton_back;
    private Button start_scan_button;
    private CircularProgressBarView pb_load_calibration, pb_load_calibration_inside;
    private TextView tv_load_calibration, tv_load_calibration_value, tv_battery_level_value, tv_update_time;
    private ViewPager2 vp_chart_pages;
    private ChartPagerAdapter chartPagerAdapter;
    private TabLayout tabLayout;
    private SharedPreferences sharedPreferences;
    private List<ScanResultLineChartFragment> charts = new ArrayList<>();
    private RecyclerView rv_function_list;
    private FunctionListAdapter functionListAdapter;
    private List<FunctionItem> functionList = new ArrayList<>();
    private Animation fadeIn, fadeOut;
    private ImageView iv_battery, iv_device;
    private ProgressBar pb_scanning;
    private int progressOfProgressbarOutside = 0;
    private int progressOfProgressbarInside = 0;
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
    // 检查是否已接收到光谱校准系数
    private Boolean downloadSpecFlag = false;
    // endregion
    // 设备是否已经连接上
    private boolean connected;
    private boolean isConnectionTimeout = true;  // todo:重要标志位，用于判断设备连接是否中途出现卡顿
    private final long CONNECTION_TIMEOUT = 12000L;
    private boolean isDeviceScanning = false;  // 设备是否正在扫描
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
    // region StatusReceiver使用的变量、常量。
    private int battery;
    private String totalLampTime = "";
    private byte[] devbyte;
    private byte[] errbyte;
    private float temperature;
    private float humidity;
    private String devStatus = "";
    private String errorStatus = "";
    // endregion
    // region DeviceInfoReceiver使用的变量、常量。
    private String model_name = "";
    private String serial_num = "";
    private String HWrev = "";
    private String Tivarev = "";
    private String Specrev = "";
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
    private byte[] Lamp_RAMPUP_ADC_DATA;
    private byte[] Lamp_AVERAGE_ADC_DATA;
    // 用于更新UI时判断加载进度条是否需要显示
    private boolean completeDeviceConnection = false;
    // 用于记录测量耗时
    private long measureTime = 0;
    // 记录当前时间
    private String currentTime = "";
    // 扫描光谱数据
    private ISCNIRScanSDK.ScanResults Scan_Spectrum_Data;
    // region 图表用数据
    // x轴数据点
    private ArrayList<String> mXValues;
    // 反射率
    private ArrayList<Entry> mReflectanceFloat;
    // 光谱强度数据（即为原始数据）
    private ArrayList<Entry> mIntensityFloat;
    // 吸光度
    private ArrayList<Entry> mAbsorbanceFloat;
    // 参比
    private ArrayList<Entry> mReferenceFloat;
    // 波长范围
    private ArrayList<Float> mWavelengthFloat;
    // endregion
    private boolean isStopped = false;
    // 常规扫描
    private ScanMethod Current_Scan_Method = ScanMethod.Normal;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(StatusReceiver, new IntentFilter(ISCNIRScanSDK.ACTION_STATUS));
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
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnLampRampUpADCReceiver, ReturnLampRampUpFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReturnLampADCAverageReceiver, ReturnLampADCAverageFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ScanDataReadyReceiver, scanDataReadyFilter);

        // endregion
    }

    private void initialComponent() {
        imageButton_back = findViewById(R.id.imageButton_back);
        start_scan_button = findViewById(R.id.start_scan_button);
        pb_load_calibration = findViewById(R.id.pb_load_calibration);
        pb_load_calibration_inside = findViewById(R.id.pb_load_calibration_inside);
        tv_load_calibration = findViewById(R.id.tv_load_calibration);
        tv_load_calibration_value = findViewById(R.id.tv_load_calibration_value);
        rv_function_list = findViewById(R.id.rv_function_list);
        tv_battery_level_value = findViewById(R.id.tv_battery_level_value);
        tv_update_time = findViewById(R.id.tv_update_time);
        iv_battery = findViewById(R.id.iv_battery);
        iv_device = findViewById(R.id.iv_device);
        vp_chart_pages = findViewById(R.id.vp_chart_pages);
        tabLayout = findViewById(R.id.tabLayout);
        pb_scanning = findViewById(R.id.pb_scanning);
        tv_battery_level_value.setText(getString(R.string.battery_level, String.valueOf(battery) + "%"));
        tv_update_time.setText("-");
        iv_device.setVisibility(View.INVISIBLE);
        pb_load_calibration.setVisibility(View.INVISIBLE);
        pb_load_calibration_inside.setVisibility(View.INVISIBLE);

        imageButton_back.setOnClickListener(this);
        start_scan_button.setOnClickListener(this);

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        start_scan_button.setEnabled(false);

        // 初始化功能列表
        rv_function_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        functionListAdapter = new FunctionListAdapter(this, functionList);
        rv_function_list.setAdapter(functionListAdapter);

        tv_battery_level_value.setText(getText(R.string.not_available));
        tv_battery_level_value.setAnimation(fadeIn);
        tv_update_time.setText(getText(R.string.not_available));
        tv_update_time.setAnimation(fadeIn);
        // 更新设备状态数据
        updateDeviceStatusUI();

        // 添加4种图表
        charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_ABSORBANCE, mAbsorbanceFloat));
        charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_REFLECTANCE, mReflectanceFloat));
        charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_INTENSITY, mIntensityFloat));
        charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_REFERENCE, mReferenceFloat));

        chartPagerAdapter = new ChartPagerAdapter(this, charts);
        vp_chart_pages.setAdapter(chartPagerAdapter);
        vp_chart_pages.setVisibility(View.INVISIBLE);
        tabLayout.setVisibility(View.INVISIBLE);
        new TabLayoutMediator(tabLayout, vp_chart_pages, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.tab_absorbance);
                        break;
                    case 1:
                        tab.setText(R.string.tab_reflectance);
                        break;
                    case 2:
                        tab.setText(R.string.tab_intensity);
                        break;
                    case 3:
                        tab.setText(R.string.tab_Reference);
                        break;
                }
            }
        }).attach();
    }


    // 初始化各类数据
    private void initialData() {
        // 初始化功能列表
        FunctionItem functionItem_1 = new FunctionItem("采集模式", "采集并预测", R.drawable.baseline_auto_graph_24, true);
        functionItem_1.setSelected(true);
        functionList.add(functionItem_1);
        functionList.add(new FunctionItem("采集模式", "仅采集光谱", R.drawable.baseline_stacked_line_chart_24, true));
        functionList.add(new FunctionItem("设备功能", "更新参比", R.drawable.baseline_switch_access_shortcut_24, true));
        // 使用出厂参比可以与其他选项共存
        FunctionItem functionItem_2 = new FunctionItem("设备功能", "使用出厂参比", R.drawable.baseline_factory_24, false);
        functionItem_2.setSelected(true);
        functionList.add(functionItem_2);
        progressOfProgressbarOutside = 0;
        progressOfProgressbarInside = 0;
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
        // todo: 后续根据是否存储了参比数据判断要不要默认选择使用出厂参比，使用设备MAC作为区分
        sharedPreferences = this.getSharedPreferences(deviceItem.getDeviceMac(), Context.MODE_PRIVATE);

        // 读取本地设备状态数据
        loadDeviceStatus();
        // 判断设备连接过程是否已经完成
        completeDeviceConnection = false;

        mXValues = new ArrayList<>();
        mReflectanceFloat = new ArrayList<>();
        mIntensityFloat = new ArrayList<>();
        mWavelengthFloat = new ArrayList<>();
        mAbsorbanceFloat = new ArrayList<>();
        mReferenceFloat = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageButton_back) {
            finish();
        } else if (view.getId() == R.id.start_scan_button) {
            // todo:判断选中的采集模式、设备功能、参比使用
            PerformScan(1000);
            // 防止重复点击
            enableAllComponent(false);
            pb_scanning.setVisibility(View.VISIBLE);
//            pb_load_calibration.setVisibility(View.VISIBLE);
        }
    }

    // 执行扫描
    private void PerformScan(long delayTime) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // 判断是否处于扫描过程中
                isDeviceScanning = true;
                // 发送广播 START_SCAN 将触发扫描
                ISCNIRScanSDK.StartScan();
            }
        }, delayTime);
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
        DeviceNotConnectedDialogFragment dialogFragment = DeviceNotConnectedDialogFragment.newInstance();
        // 确保此时用户没有退出当前Activity
        if (!isFinishing() && !isDestroyed() && !isStopped) {
            dialogFragment.show(getSupportFragmentManager(), "连接超时");
        } else {
            // 如果弹窗时软件不在前台，则直接finish.
            finish();
        }

    }

    // 启用或停用界面组件
    private void enableAllComponent(boolean enable) {
        imageButton_back.setEnabled(enable);
        start_scan_button.setEnabled(enable);
        tabLayout.setEnabled(enable);
        vp_chart_pages.setEnabled(enable);
        functionListAdapter.setClickable(enable);
        if (enable) {
            tabLayout.setVisibility(View.VISIBLE);
            vp_chart_pages.setVisibility(View.VISIBLE);
            pb_scanning.setVisibility(View.INVISIBLE);
        } else {
            tabLayout.setVisibility(View.INVISIBLE);
            vp_chart_pages.setVisibility(View.INVISIBLE);
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

    // 更新布局中的设备信息
    private void updateDeviceStatusUI() {
        tv_battery_level_value.setText(getString(R.string.battery_level, String.valueOf(battery) + "%"));
        // todo:规范参比更新时间描述
        tv_update_time.setText("-");
        tv_battery_level_value.startAnimation(fadeIn);
        tv_update_time.startAnimation(fadeIn);
        // 更改电池图标
        iv_battery.setImageResource(upDateBatteryIcon(battery));

    }

    // 存储设备状态信息。
    private void saveDeviceStatus() {
        Log.d(TAG, "扫描页-saveDeviceStatus called.存储了设备信息。");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_device_battery), battery);
        editor.putFloat(getString(R.string.pref_device_temperature), temperature);
        editor.putFloat(getString(R.string.pref_device_humidity), humidity);
        editor.putString(getString(R.string.pref_device_totalLampTime), totalLampTime);
        // todo:规范参考更新时间
//        editor.putString(getString(R.string.pref_app_reference_update_time), "-");
        editor.apply();
    }

    private void loadDeviceStatus() {
        Log.d(TAG, "扫描页-loadDeviceStatus called.读取了设备信息。");
        battery = sharedPreferences.getInt(getString(R.string.pref_device_battery), 0);
        temperature = sharedPreferences.getFloat(getString(R.string.pref_device_temperature), 0);
        humidity = sharedPreferences.getFloat(getString(R.string.pref_device_humidity), 0);
        totalLampTime = sharedPreferences.getString(getString(R.string.pref_device_totalLampTime), "-");
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

    // 设定设备物理按钮状态
    private void setDeviceButtonStatus() {
        Log.d(TAG, "扫描页-setDeviceButtonStatus called");
        // 校验设备波长类型、fw_level
        if (isExtendVer_PLUS || isExtendVer || fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_1) > 0) {
            boolean isLockButton = getBooleanPref(mContext, ISCNIRScanSDK.SharedPreferencesKeys.LockButton, false);
            Log.d(TAG, "扫描页-setDeviceButtonStatus，isLockButton：" + isLockButton);
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

    // region 根据Tiva版本获取 FW level
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

    // 改变灯状态
    private void changeLampState() {
        if (warmUp) {
            ISCNIRScanSDK.ControlLamp(ISCNIRScanSDK.LampState.AUTO);
            warmUp = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "扫描页-onPause called");

        // todo:按照SDK中的建议，如果onPause不是为了跳转其他界面，则应该finish()当前Activity。

    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "扫描页-onDestroy called");
        // 改变光源状态，避免软件退出后光源常亮
        changeLampState();
        // todo:解绑服务、取消广播接收器的注册
        unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusReceiver);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnLampRampUpADCReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ReturnLampADCAverageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ScanDataReadyReceiver);
    }

    public enum LampInfo {
        WarmDevice, ManualLamp, CloseWarmUpLampInScan
    }

    public enum ScanMethod {
        Normal, QuickSet, Manual, Maintain
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

                    // 同步时间，以下载校准参数和矩阵。
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
                // UI
                showLoadAnimation(true);

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
                    //UI
                    showLoadAnimation(true);
                    // 本地获取到校准系数、矩阵，进度设置为100.
                    progressOfProgressbarOutside = 100;
                    pb_load_calibration.setProgress(progressOfProgressbarOutside, 1000);
                    tv_load_calibration.setText(getString(R.string.calibration_coefficient_and_matrix_read_from_local));

                } else {
                    // 本次启动没有存储校准参数，同步时间并下载校准系数和校准矩阵
                    ISCNIRScanSDK.ShouldDownloadCoefficient = true;
                    ISCNIRScanSDK.SetCurrentTime();
                    // UI
                    showLoadAnimation(true);
                    Log.d(TAG, "扫描页-NotifyCompleteReceiver本地未找到校准数据，NotifyCompleteReceiver SetCurrentTime() called.");
                }

            }
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
                // 更新进度条进度
                progressOfProgressbarOutside = (refCoeffDataProgressCurrentProgress / refCoeffDataProgressTotalSize) * 50;
                tv_load_calibration_value.setText(getString(R.string.connecting_progress, progressOfProgressbarOutside));

                String receivingProgress = getString(R.string.calibration_coefficient_receiving, String.valueOf(refCoeffDataProgressCurrentProgress), String.valueOf(refCoeffDataProgressTotalSize));
                tv_load_calibration.setText(receivingProgress);
                Log.d(TAG, "扫描页-RefCoeffDataProgressReceiver中EXTRA_REF_CAL_COEFF_SIZE_PACKET为false，当前数据包大小为：" + currentSize);
            }
            if (refCoeffDataProgressCurrentProgress == refCoeffDataProgressTotalSize) {
//                pb_load_calibration.setProgress(progressOfProgressbarOutside, 1000);
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
                // 此处初始化进度条，获得进度条总长度
                calMatrixDataProgressTotalSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                calMatrixDataProgressCurrentProgress = 0;
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE_PACKET为true，当前为第一个数据包。");
                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中接收到数据包总大小为：" + calMatrixDataProgressTotalSize);
            } else {
                // 此处更新进度
                int currentSize = intent.getIntExtra(ISCNIRScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
                calMatrixDataProgressCurrentProgress += currentSize;
                String receivingProgress = getString(R.string.calibration_matrix_receiving, String.valueOf(calMatrixDataProgressCurrentProgress), String.valueOf(calMatrixDataProgressTotalSize));
                tv_load_calibration.setText(receivingProgress);

                // 更新进度
                progressOfProgressbarOutside = 50 + (refCoeffDataProgressCurrentProgress / refCoeffDataProgressTotalSize) * 50;
                tv_load_calibration_value.setText(getString(R.string.connecting_progress, progressOfProgressbarOutside));

                Log.d(TAG, "扫描页-CalMatrixDataProgressReceiver中EXTRA_REF_CAL_MATRIX_SIZE为false，当前数据包大小为：" + currentSize);
            }

            if (calMatrixDataProgressCurrentProgress == calMatrixDataProgressTotalSize) {
                pb_load_calibration.setProgress(progressOfProgressbarOutside, 1000);
                tv_load_calibration.setText(getString(R.string.calibration_matrix_received));
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);
                // 当接收完毕后，调用 ISCNIRScanSDK.GetActiveConfig();，SDK将发送broadcast GET_ACTIVE_CONF，获取活动配置的索引。
                ISCNIRScanSDK.GetActiveConfig();
            }

        }
    }

    // 此广播接收器可以获取校准系数和校准矩阵具体的数值，REF_CONF_DATA
    public class RefDataReadyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 设备连接未超时
            isConnectionTimeout = false;
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
            // 设备连接未超时
            isConnectionTimeout = false;

            receivedConfSize++;
            scanConfigByteList.add(intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DATA));
            scanConfigList.add(ISCNIRScanSDK.scanConf);
            tv_load_calibration.setText(getString(R.string.active_scan_config_receiving, String.valueOf(receivedConfSize), String.valueOf(storedConfSize)));

            // 更新进度
            progressOfProgressbarInside = (receivedConfSize / storedConfSize) * 100;
            tv_load_calibration_value.setText(getString(R.string.connecting_progress, progressOfProgressbarInside));

            Log.d(TAG, "扫描页-ScanConfReceiver获取到扫描配置：receivedConfSize:" + receivedConfSize + "\n" +
                    "scanConfigByteList.size:" + scanConfigByteList.size() + "\nscanConfigList.size:" + scanConfigList.size() + "\n" +
                    "storedConfSize:" + storedConfSize);
            // 配置文件接收完成
            if (receivedConfSize == storedConfSize) {
                // 从ScanConfigList和ScanConfig_Byte_List中根据活动配置ActiveConfigIndex取出配置信息
                tv_load_calibration.setText(getString(R.string.active_scan_config_received));
                tv_load_calibration.setAnimation(fadeIn);
                tv_load_calibration.startAnimation(fadeIn);
                pb_load_calibration_inside.setProgress(progressOfProgressbarInside, 1000);

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
            Log.d(TAG, "扫描页-ReturnMFGNumReceiver called，设备制造序列号MFG_NUM：" + Arrays.toString(MFG_NUM));
            // 当Tiva为 2.5.x 时，调用GetHWModel。
            if ((!isExtendVer_PLUS && !isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4) >= 0) || (isExtendVer && fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_3) >= 0)) {
                Log.d(TAG, "扫描页-ReturnMFGNumReceiver，设备为标准光谱，fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4)>=0，具体为:" + fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4));
                ISCNIRScanSDK.GetHWModel();
            } else {
                Log.d(TAG, "扫描页-ReturnMFGNumReceiver，设备为标准光谱，fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4)<0，具体为:" + fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_4));

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
            // todo: 未来有必要的前提下，实现旧版本兼容;现有设备 Tivarev:2.4.7、fw_level_standard：LEVEL_3。

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
            // 判断设备是否已经激活
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
                Log.d(TAG, "扫描页-ReturnReadActivateStatusReceiver，设备激活状态已存储：Activated.");

                // 尝试获取设备状态，延时3秒防止挂起
                mHandler.postDelayed(() -> {
                    Log.d(TAG, "扫描页-ReturnReadActivateStatusReceiver，开始获取设备状态信息。");

                    tv_load_calibration.setText(getString(R.string.device_connected));
                    completeDeviceConnection = true;
                    ISCNIRScanSDK.GetDeviceStatus();
                }, 3000);
            } else {
                // todo:检查本地是否有存储许可证（密钥长度为24）
                Toast.makeText(mContext, "设备激活失败！", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // 获取设备状态，包括电池余量、温度湿度等。
    public class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-StatusReceiver called");
            battery = intent.getIntExtra(ISCNIRScanSDK.EXTRA_BATT, 0);
            temperature = intent.getFloatExtra(ISCNIRScanSDK.EXTRA_TEMP, 0);
            humidity = intent.getFloatExtra(ISCNIRScanSDK.EXTRA_HUMID, 0);
            long lampTime = intent.getLongExtra(ISCNIRScanSDK.EXTRA_LAMPTIME, 0);
            totalLampTime = GetLampTimeString(lampTime);

            devStatus = intent.getStringExtra(ISCNIRScanSDK.EXTRA_DEV_STATUS);
            errorStatus = intent.getStringExtra(ISCNIRScanSDK.EXTRA_ERR_STATUS);
            devbyte = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_DEV_STATUS_BYTE);
            errbyte = intent.getByteArrayExtra(ISCNIRScanSDK.EXTRA_ERR_BYTE);
            Log.e(TAG, "battery:" + battery + "\nTotalLampTime:" + totalLampTime + "\ndevByte:" + Arrays.toString(devbyte));
            // 更新界面设备状态信息UI
            updateDeviceStatusUI();
            // todo:存储设备状态信息。
            saveDeviceStatus();
            // 用于判断是否是设备第一次和软件的连接过程
            if (completeDeviceConnection) {
                // UI
                showLoadAnimation(false);

                vp_chart_pages.setVisibility(View.VISIBLE);
                enableAllComponent(true);
                completeDeviceConnection = false;
                // 弹窗提醒连接成功
                if (!isFinishing() && !isDestroyed() && !isStopped) {
                    GeneralMessageDialogFragment dialogFragment = GeneralMessageDialogFragment.newInstance(
                            GeneralMessageDialogFragment.MESSAGE_TYPE_CHECK, getString(R.string.connection_successful_dialog_title),
                            getString(R.string.connection_successful_dialog_content));
                    dialogFragment.setDialogFinishActivity(false);
                    dialogFragment.show(getSupportFragmentManager(), "Device connection successful!");
                }
            }
            // 当前设备Tivarev:2.4.7、fw_level_standard：LEVEL_3、isExtendVer_PLUS:false、isExtendVer:false
            // 请求获取灯源ADC
            if (isExtendVer_PLUS ||
                    (isExtendVer && (fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_2) == 0 || fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_4) == 0))
                    || (!isExtendVer && (fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_3) == 0 || fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_5) == 0))) {
                Log.d(TAG, "扫描页-固件版本f符合，正在请求获取灯源ADC。");
                ISCNIRScanSDK.GetScanLampRampUpADC();
            } else {
                Log.e(TAG, "扫描页-固件版本不符合，call DoScanComplete();");
            }

        }
    }

    // endregion

    // 获取灯源ADC（模数转换）
    public class ReturnLampRampUpADCReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-ReturnLampRampUpADCReceiver called.");
            Lamp_RAMPUP_ADC_DATA = intent.getByteArrayExtra(ISCNIRScanSDK.LAMP_RAMPUP_DATA);
            ISCNIRScanSDK.GetLampADCAverage();
        }
    }

    // 获取平均灯源ADC（模数转换）
    public class ReturnLampADCAverageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-ReturnLampADCAverageReceiver called");
            Lamp_AVERAGE_ADC_DATA = intent.getByteArrayExtra(ISCNIRScanSDK.LAMP_ADC_AVERAGE_DATA);
            if (isExtendVer_PLUS || (!isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_5) == 0)
                    || (isExtendVer && fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_4) == 0)) {
                // 当前设备Tivarev:2.4.7、fw_level_standard：LEVEL_3、isExtendVer_PLUS:false、isExtendVer:false
                // 开发用设备不符合条件
                ISCNIRScanSDK.GetLampADCTimeStamp();
            } else {
                Log.e(TAG, "扫描页-ReturnLampADCAverageReceiver，已获取Lamp_AVERAGE_ADC_DATA。设备固件不符合条件，准备执行执行DoScanComplete");
                // todo:scanComplete。
                scanComplete();
            }
        }
    }

    private void scanComplete() {
    /* todo:
        1.更新设备物理按钮锁定状态
        2.检查设备激活状态，未激活则关闭特定功能
        3.将扫描数据存储进NirSpectralData对象中
        4.获取光谱预测数据
        5.根据选择的采集模式，处理光谱数据，比如：更新参比
        6.更新UI状态，包括图表、预测结果、是否保存结果等
      */
        if (isDeviceScanning) {
            Log.d(TAG, "扫描页-scanComplete called.");
            chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_ABSORBANCE, mAbsorbanceFloat);
            chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_REFLECTANCE, mReflectanceFloat);
            chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_INTENSITY, mIntensityFloat);
            chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_REFERENCE, mReferenceFloat);
            enableAllComponent(true);
            pb_load_calibration.setVisibility(View.INVISIBLE);
            // 判断是否处于扫描过程中
            isDeviceScanning = false;
        }

    }

    // 将扫描数据写入为csv文件，发送至服务器并预测
    private void writeCSV(ISCNIRScanSDK.ScanResults scanResults) {
        boolean HaveError = false;
        int scanType;
        int numSections;
        String[] widthnm = {"", "", "2.34", "3.51", "4.68", "5.85", "7.03", "8.20", "9.37", "10.54", "11.71", "12.88", "14.05", "15.22", "16.39", "17.56", "18.74"
                , "19.91", "21.08", "22.25", "23.42", "24.59", "25.76", "26.93", "28.10", "29.27", "30.44", "31.62", "32.79", "33.96", "35.13", "36.30", "37.47", "38.64", "39.81"
                , "40.98", "42.15", "43.33", "44.50", "45.67", "46.84", "48.01", "49.18", "50.35", "51.52", "52.69", "53.86", "55.04", "56.21", "57.38", "58.55", "59.72", "60.89"};
        String[] widthnm_plus = {"", "", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"
                , "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34"
                , "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61"};
        String[] exposureTime = {"0.635", "1.27", " 2.54", " 5.08", "15.24", "30.48", "60.96"};
        int index = 0;
        double temp;
        double humidity;
        //-------------------------------------------------
        String newdate = "";
        String[][] CSV = new String[35][15];
        for (int i = 0; i < 35; i++)
            for (int j = 0; j < 15; j++)
                CSV[i][j] = ",";

        numSections = ISCNIRScanSDK.ScanConfigInfo.numSections[0];
        scanType = ISCNIRScanSDK.ScanConfigInfo.scanType[0];
        //----------------------------------------------------------------
        String configname = PasswordUtils.getBytetoString(ISCNIRScanSDK.ScanConfigInfo.configName);
        // 写入参比更新时间
        // todo:当更新参比时，此处存储当前的时间（注意增加判断是否“选中增加参比”）
        if (Current_Scan_Method == ScanMethod.Maintain) {
            configname = "Reference";
            Date datetime = new Date();
            SimpleDateFormat format_1 = new SimpleDateFormat("yy/MM/dd", Locale.getDefault());
            SimpleDateFormat format_2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            newdate = format_1.format(datetime) + "T" + format_2.format(datetime);
            CSV[14][8] = newdate;
        } else {
            // 使用存储的时间
            CSV[14][8] = ISCNIRScanSDK.ReferenceInfo.refday[0] + "/" + ISCNIRScanSDK.ReferenceInfo.refday[1]
                    + "/" + ISCNIRScanSDK.ReferenceInfo.refday[2] + "T" + ISCNIRScanSDK.ReferenceInfo.refday[3]
                    + ":" + ISCNIRScanSDK.ReferenceInfo.refday[4] + ":" + ISCNIRScanSDK.ReferenceInfo.refday[5];
        }
        //region Section information field names
        CSV[0][0] = "***Scan Config Information***,";
        CSV[1][0] = "Scan Config Name:,";
        CSV[2][0] = "Scan Config Type:,";
        CSV[3][0] = "Section Config Type:,";
        CSV[4][0] = "Start Wavelength (nm):,";
        CSV[5][0] = "End Wavelength (nm):,";
        CSV[6][0] = "Pattern Width (nm):,";
        CSV[7][0] = "Exposure (ms):,";
        CSV[8][0] = "Digital Resolution:,";
        CSV[9][0] = "Num Repeats:,";
        CSV[10][0] = "PGA Gain:,";
        CSV[11][0] = "System Temp (C):,";
        CSV[12][0] = "Humidity (%):,";
        CSV[13][0] = "Battery Capacity (%):,";
        if ((isExtendVer && fw_level_ext.compareTo(ISCNIRScanSDK.FW_LEVEL_EXT.LEVEL_EXT_2) >= 0) || (!isExtendVer && fw_level_standard.compareTo(ISCNIRScanSDK.FW_LEVEL_STANDARD.LEVEL_3) >= 0)) {
            // 当前设备满足
            CSV[14][0] = "Lamp ADC:,";
        } else {
            CSV[14][0] = "Lamp Indicator:,";
        }
        CSV[15][0] = "Data Date-Time:,";
        CSV[16][0] = "Total Measurement Time in sec:,";

        CSV[1][1] = configname + ",";
        CSV[2][1] = "Slew,";
        CSV[2][2] = "Num Section:,";
        CSV[2][3] = numSections + ",";
        // 写入扫描模式信息(Column、Hadamard）
        for (int i = 0; i < numSections; i++) {
            if (ISCNIRScanSDK.ScanConfigInfo.sectionScanType[i] == 0) {
                CSV[3][i + 1] = "Column,";
            } else {
                CSV[3][i + 1] = "Hadamard,";
            }
            CSV[4][i + 1] = ISCNIRScanSDK.ScanConfigInfo.sectionWavelengthStartNm[i] + ",";
            CSV[5][i + 1] = ISCNIRScanSDK.ScanConfigInfo.sectionWavelengthEndNm[i] + ",";
            index = ISCNIRScanSDK.ScanConfigInfo.sectionWidthPx[i];
            if (isExtendVer_PLUS) {
                // 当前设备不满足
                CSV[6][i + 1] = widthnm_plus[index] + ",";
            } else {
                CSV[6][i + 1] = widthnm[index] + ",";
            }
            index = ISCNIRScanSDK.ScanConfigInfo.sectionExposureTime[i];
            CSV[7][i + 1] = exposureTime[index] + ",";
            CSV[8][i + 1] = ISCNIRScanSDK.ScanConfigInfo.sectionNumPatterns[i] + ",";
            CSV[9][1] = ISCNIRScanSDK.ScanConfigInfo.sectionNumRepeats[0] + ",";
            CSV[10][1] = ISCNIRScanSDK.ScanConfigInfo.pga[0] + ",";
            temp = ISCNIRScanSDK.ScanConfigInfo.systemp[0];
            temp = temp / 100;
            CSV[11][1] = temp + ",";
            humidity = ISCNIRScanSDK.ScanConfigInfo.syshumidity[0];
            humidity = humidity / 100;
            CSV[12][1] = humidity + ",";
            CSV[13][1] = battery + ",";
            CSV[14][1] = ISCNIRScanSDK.ScanConfigInfo.lampintensity[0] + ",";
            CSV[15][1] = ISCNIRScanSDK.ScanConfigInfo.day[0] + "/" + ISCNIRScanSDK.ScanConfigInfo.day[1]
                    + "/" + ISCNIRScanSDK.ScanConfigInfo.day[2] + "T" + ISCNIRScanSDK.ScanConfigInfo.day[3]
                    + ":" + ISCNIRScanSDK.ScanConfigInfo.day[4] + ":" + ISCNIRScanSDK.ScanConfigInfo.day[5] + ",";
            CSV[16][1] = Double.toString((double) measureTime / 1000);
        }
        //endregion
        //region General Information
        CSV[17][0] = "***General Information***,";
        CSV[18][0] = "Model Name:,";
        CSV[19][0] = "Serial Number:,";
        CSV[20][0] = "APP Version:,";
        CSV[21][0] = "TIVA Version:,";
        CSV[22][0] = "UUID:,";
        CSV[23][0] = "Main Board Version:,";
        CSV[24][0] = "Detector Board Version:,";

        CSV[18][1] = model_name + ",";
        CSV[19][1] = serial_num + ",";
        String mfg_num = getMfg_num();
        CSV[19][2] = mfg_num + ",";
        String version = "";
        int versionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "扫描页-writeCSV，PackageManager.NameNotFoundException：" + e);
        }
        CSV[20][1] = version + "." + Integer.toString(versionCode);
        CSV[21][1] = Tivarev + ",";
        CSV[22][1] = uuid + ",";
        String[] split_hw = HWrev.split("\\.");
        CSV[23][1] = split_hw[0] + ",";
        CSV[24][1] = split_hw[2] + ",";
        // endregion
        //region Reference Scan Information
        CSV[0][7] = "***Reference Scan Information***,";
        CSV[1][7] = "Scan Config Name:,";
        CSV[2][7] = "Scan Config Type:,";
        CSV[3][7] = "Section Config Type:,";
        CSV[4][7] = "Start Wavelength (nm):,";
        CSV[5][7] = "End Wavelength (nm):,";
        CSV[6][7] = "Pattern Width (nm):,";
        CSV[7][7] = "Exposure (ms):,";
        CSV[8][7] = "Digital Resolution:,";
        CSV[9][7] = "Num Repeats:,";
        CSV[10][7] = "PGA Gain:,";
        CSV[11][7] = "System Temp (C):,";
        CSV[12][7] = "Humidity (%):,";
        CSV[13][7] = "Lamp Indicator:,";
        CSV[14][7] = "Data Date-Time:,";
        if (PasswordUtils.getBytetoString(ISCNIRScanSDK.ReferenceInfo.refconfigName).equals("SystemTest")) {
            CSV[1][8] = "Built-in Factory Reference";
        } else {
            CSV[1][8] = "Built-in User Reference";
        }
        CSV[2][8] = "Slew,";
        CSV[2][9] = "Num Section:,";
        CSV[2][10] = "1,";
        if (ISCNIRScanSDK.ReferenceInfo.refconfigtype[0] == 0) {
            CSV[3][8] = "Column,";
        } else {
            CSV[3][8] = "Hadamard,";
        }
        CSV[4][8] = Double.toString(ISCNIRScanSDK.ReferenceInfo.refstartwav[0]);
        CSV[5][8] = Double.toString(ISCNIRScanSDK.ReferenceInfo.refendwav[0]);
        index = ISCNIRScanSDK.ReferenceInfo.width[0];
        if (isExtendVer_PLUS) {
            CSV[6][8] = widthnm_plus[index] + ",";
        } else {
            CSV[6][8] = widthnm[index] + ",";
        }
        index = ISCNIRScanSDK.ReferenceInfo.refexposuretime[0];
        CSV[7][8] = exposureTime[index] + ",";
        CSV[8][8] = ISCNIRScanSDK.ReferenceInfo.numpattren[0] + ",";
        CSV[9][8] = ISCNIRScanSDK.ReferenceInfo.numrepeat[0] + ",";
        CSV[10][8] = Integer.toString(ISCNIRScanSDK.ReferenceInfo.refpga[0]);
        temp = ISCNIRScanSDK.ReferenceInfo.refsystemp[0];
        temp = temp / 100;
        CSV[11][8] = temp + ",";
        humidity = (double) ISCNIRScanSDK.ReferenceInfo.refsyshumidity[0] / 100;
        CSV[12][8] = Double.toString(humidity);
        CSV[13][8] = ISCNIRScanSDK.ReferenceInfo.reflampintensity[0] + ",";
        // endregion

        // 校准系数
    }

    private @NonNull String getMfg_num() {
        String mfg_num = "";
        try {
            mfg_num = new String(MFG_NUM, StandardCharsets.ISO_8859_1);
            if (!mfg_num.contains("70UB1") && !mfg_num.contains("95UB1"))
                mfg_num = "";
            else if (mfg_num.contains("95UB1"))//Extended 19 words, standard 18 words
            {
                if (isExtendVer && mfg_num.length() >= 19)
                    mfg_num = mfg_num.substring(0, 19);
                else if (!isExtendVer_PLUS && !isExtendVer && mfg_num.length() >= 18)
                    mfg_num = mfg_num.substring(0, 18);
                else
                    mfg_num = mfg_num.substring(0, mfg_num.length() - 2);
            }
        } catch (Exception e) {
            mfg_num = "";
        }
        return mfg_num;
    }

    //    用于处理扫描数据和正确设置图形的自定义接收器（应调用ISCNIRScanSDK.StartScan（））
    public class ScanDataReadyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "扫描页-ScanDataReadyReceiver called.");
            long endTime = System.currentTimeMillis();
            measureTime = endTime - ISCNIRScanSDK.startScanTime;
            if (Interpret_length < 0) {
                Toast.makeText(mContext, "设备异常，请重新连接！", Toast.LENGTH_LONG).show();
                finish();
            } else {
                // 获取扫描光谱数据
                Scan_Spectrum_Data = new ISCNIRScanSDK.ScanResults(Interpret_wavelength, Interpret_intensity, Interpret_uncalibratedIntensity, Interpret_length);
                Log.d(TAG, "扫描页-Scan_Spectrum_Data扫描数据成功获取，measureTime:" + measureTime + "\nScan_Spectrum_Data.length:" + Scan_Spectrum_Data.getLength());
                mReflectanceFloat.clear();
                mIntensityFloat.clear();
                mAbsorbanceFloat.clear();
                mReferenceFloat.clear();
                mWavelengthFloat.clear();

                for (int i = 0; i < Scan_Spectrum_Data.getLength(); i++) {
                    mXValues.add(String.format(getString(R.string.scan_wave_length), ISCNIRScanSDK.ScanResults.getSpatialFreq(mContext, Scan_Spectrum_Data.getWavelength()[i])));
                    mIntensityFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[i], (float) Scan_Spectrum_Data.getUncalibratedIntensity()[i]));
                    mAbsorbanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[i], (-1) * (float) Math.log10((double) Scan_Spectrum_Data.getUncalibratedIntensity()[i] / (double) Scan_Spectrum_Data.getIntensity()[i])));
                    mReflectanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[i], (float) Scan_Spectrum_Data.getUncalibratedIntensity()[i] / Scan_Spectrum_Data.getIntensity()[i]));
                    mWavelengthFloat.add((float) Scan_Spectrum_Data.getWavelength()[i]);
                    mReferenceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[i], (float) Scan_Spectrum_Data.getIntensity()[i]));
                }
                // 初始化图表横、纵坐标的范围
                initializesTableRange();
                // 获取当前时间
                SimpleDateFormat filesimpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                currentTime = filesimpleDateFormat.format(new Date());
                if (warmUp) {
                    // 如果灯源此时在预热状态，则改回AUTO，关闭光源。
                    ISCNIRScanSDK.ControlLamp(ISCNIRScanSDK.LampState.AUTO);
                    lampInfo = LampInfo.CloseWarmUpLampInScan;
                } else {
                    // 开始获取设备信息。
                    ISCNIRScanSDK.GetDeviceStatus();
                }
            }
        }

        // 初始化图表横、纵坐标的范围
        private void initializesTableRange() {
            minWavelength = mWavelengthFloat.get(0);
            maxWavelength = mWavelengthFloat.get(0);

            for (Float f : mWavelengthFloat) {
                if (f < minWavelength) minWavelength = f;
                if (f > maxWavelength) maxWavelength = f;
            }

            minAbsorbance = mAbsorbanceFloat.get(0).getY();
            maxAbsorbance = mAbsorbanceFloat.get(0).getY();
            for (Entry e : mAbsorbanceFloat) {
                if (e.getY() < minAbsorbance || Float.isNaN(minAbsorbance))
                    minAbsorbance = e.getY();
                if (e.getY() > maxAbsorbance || Float.isNaN(maxAbsorbance))
                    maxAbsorbance = e.getY();
            }

            minReflectance = mReflectanceFloat.get(0).getY();
            maxReflectance = mReflectanceFloat.get(0).getY();

            for (Entry e : mReflectanceFloat) {
                if (e.getY() < minReflectance || Float.isNaN(minReflectance))
                    minReflectance = e.getY();
                if (e.getY() > maxReflectance || Float.isNaN(maxReflectance))
                    maxReflectance = e.getY();
            }
            if (minReflectance == 0 && maxReflectance == 0) {
                maxReflectance = 2;
            }

            minIntensity = mIntensityFloat.get(0).getY();
            maxIntensity = mIntensityFloat.get(0).getY();

            for (Entry e : mIntensityFloat) {
                if (e.getY() < minIntensity || Float.isNaN(minIntensity))
                    minIntensity = e.getY();
                if (e.getY() > maxIntensity || Float.isNaN(maxIntensity))
                    maxIntensity = e.getY();
            }
            if (minIntensity == 0 && maxIntensity == 0) {
                maxIntensity = 1000;
            }

            minReference = mReferenceFloat.get(0).getY();
            maxReference = mReferenceFloat.get(0).getY();

            for (Entry e : mReferenceFloat) {
                if (e.getY() < minReference || Float.isNaN(minReference))
                    minReference = e.getY();
                if (e.getY() > maxReference || Float.isNaN(maxReference))
                    maxReference = e.getY();
            }
            if (minReference == 0 && maxReference == 0) {
                maxReference = 1000;
            }
        }
    }

    private void showLoadAnimation(boolean enable) {
        if (enable) {
            iv_device.setVisibility(View.VISIBLE);
            iv_device.setAnimation(fadeIn);
            iv_device.startAnimation(fadeIn);

            pb_load_calibration.setVisibility(View.VISIBLE);
            pb_load_calibration_inside.setVisibility(View.VISIBLE);

            pb_load_calibration.setAnimation(fadeIn);
            pb_load_calibration_inside.setAnimation(fadeIn);

            pb_load_calibration.startAnimation(fadeIn);
            pb_load_calibration_inside.startAnimation(fadeIn);

            tv_load_calibration.setAnimation(fadeIn);
            tv_load_calibration.startAnimation(fadeIn);
        } else {
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    pb_load_calibration.setVisibility(View.GONE);
                    pb_load_calibration_inside.setVisibility(View.GONE);
                    iv_device.setVisibility(View.GONE);
                    tv_load_calibration.setVisibility(View.GONE);
                    tv_load_calibration_value.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            iv_device.startAnimation(fadeOut);
            pb_load_calibration.startAnimation(fadeOut);
            pb_load_calibration_inside.startAnimation(fadeOut);
            tv_load_calibration.startAnimation(fadeOut);
            tv_load_calibration_value.startAnimation(fadeOut);
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
            mHandler = new Handler(getMainLooper());
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

                    boolean connect = mNanoBLEService.connect(device.getAddress());
                    Log.d(TAG, "扫描页-mPreferredLeScanCallback的onScanResult 成功获取preferredNano和name"
                            + preferredNano + "，name:" + name + "。已连接设备！connected =" + connect);
                    if (!connected) {
                        checkDeviceConnections();
                    }
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
                    if (!connected) {
                        checkDeviceConnections();
                    }
                    connected = true;
                    scanLeDevice(false);
                }
            }
        }
    };

    private void checkDeviceConnections() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnectionTimeout) {
                    Toast.makeText(mContext, "连接意外中断，请重试！", Toast.LENGTH_LONG).show();
                    // 使用弹窗
                    if (!isFinishing() && !isDestroyed() && !isStopped) {
                        GeneralMessageDialogFragment dialogFragment = GeneralMessageDialogFragment.newInstance(
                                GeneralMessageDialogFragment.MESSAGE_TYPE_ERROR, getString(R.string.connection_interruption_dialog_title),
                                getString(R.string.connection_interruption_dialog_content));
                        dialogFragment.show(getSupportFragmentManager(), "Unexpected connection interruption!");
                    } else {
                        finish();
                    }
                }

            }
        }, CONNECTION_TIMEOUT);
    }

}
package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: 2024/8/17 完成光谱的读取、删除

public class DeviceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DeviceDetailsActivity";
    private static String DEVICE_NAME = "NIR";
    // region UI布局
    private ToggleButton not_preheat_toggle;
    private ImageButton scan, imageButton_back, imageButton_menu;
    private ConstraintLayout device_connection_layout;
    private DeviceItem deviceItem;
    private TextView light_usage_duration, humidity_value, battery_level, tv_device_mac,
            tv_device_name, connect_text, spectral_reference_update_date_value, number_of_spectra_collected_value, recent_spectral_data_list_empty;
    private ProgressBar scan_progressbar, progressBar;
    private ImageView connect_btn, battery_image;
    private RecyclerView rv_recent_spectral_data_list;
    private RecentSpectralDataListAdapter spectralDataListAdapter;

    // endregion
    private Animation fadeIn, fadeOut;
    private Handler handler;
    private boolean warmUp = false;
    private SharedPreferences sharedPreferences, userProfileSharedPreferences;
    private List<DeviceDetailMenuItems> menuItems;
    // region 设备状态
    private int battery;
    private String totalLampTime = "";
    private String referenceUpdateDate = "-";
    private int numberOfSpectraCollected = 0;
    // endregion
    private String userPhoneNumber, deviceMac;
    private Context mContext;

    // 设备信息修改
    private List<DeviceItem> itemList;
    private TextView device_type_input, device_info_input, device_list_empty;
    private EditText device_name_input;
    private int devicePosition = -1;
    private DeviceItem deviceItemFromLocal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "设备详情页-onCreate called!");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_details);
        mContext = this;

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

    @Override
    protected void onResume() {
        super.onResume();
        // 更新设备状态信息、光谱数据。
        updateDeviceData();
    }

    private void initialData() {
        Log.d(TAG, "设备详情页-initialData called!");
//        SpectralDataUtils.LoadSpectralFileMapFromFile();
        handler = new Handler();
        // 初始化数据
        deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        sharedPreferences = this.getSharedPreferences(Objects.requireNonNull(deviceItem).getDeviceMac(), Context.MODE_PRIVATE);
        // 从本地文件读取光谱索引数据
        userProfileSharedPreferences = this.getSharedPreferences("default", MODE_PRIVATE);
        userPhoneNumber = userProfileSharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        deviceMac = deviceItem.getDeviceMac();
        SpectralDataUtils.loadSpectralFileMapFromFile(this, userPhoneNumber, deviceMac);

        // 获取设备列表本地的序列化数据
        itemList = new ArrayList<>();
        itemList = SpectralDataUtils.readDeviceListFromFile(this, userPhoneNumber);
        // 获取当前设备在列表中的位置
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).equals(deviceItem)) {
                devicePosition = i;
            }
        }


        // 更新光谱数量
        numberOfSpectraCollected = SpectralDataUtils.userSpectralFileMap.size();

        menuItems = new ArrayList<>();
        menuItems.add(new DeviceDetailMenuItems("设备详情"));
        menuItems.add(new DeviceDetailMenuItems("删除本地参比"));
        menuItems.add(new DeviceDetailMenuItems("删除当前设备"));
        menuItems.add(new DeviceDetailMenuItems("检索光谱"));
        menuItems.add(new DeviceDetailMenuItems("连接设备"));
    }

    private void initialComponent() {
        Log.d(TAG, "设备详情页-initialComponent called!");
        // 实例化组件
        not_preheat_toggle = findViewById(R.id.not_preheat_toggle);
        scan = findViewById(R.id.scan);
        device_connection_layout = findViewById(R.id.device_connection_layout);
        light_usage_duration = findViewById(R.id.light_usage_duration);
        battery_level = findViewById(R.id.battery_level);
        scan_progressbar = findViewById(R.id.scan_progressbar);
        progressBar = findViewById(R.id.progressBar);
        imageButton_back = findViewById(R.id.imageButton_back);
        imageButton_menu = findViewById(R.id.imageButton_menu);
        tv_device_mac = findViewById(R.id.tv_device_mac);
        tv_device_name = findViewById(R.id.tv_device_name);
        connect_btn = findViewById(R.id.connect_btn);
        battery_image = findViewById(R.id.battery_image);
        connect_text = findViewById(R.id.connect_text);
        rv_recent_spectral_data_list = findViewById(R.id.rv_recent_spectral_data_list);
        spectral_reference_update_date_value = findViewById(R.id.spectral_reference_update_date_value);
        number_of_spectra_collected_value = findViewById(R.id.number_of_spectra_collected_value);
        recent_spectral_data_list_empty = findViewById(R.id.recent_spectral_data_list_empty);

        tv_device_mac.setText(deviceItem.getDeviceMac());
        tv_device_name.setText(deviceItem.getDeviceName());

        // 为光谱数据列表设置监听器
        rv_recent_spectral_data_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        spectralDataListAdapter = new RecentSpectralDataListAdapter(this);
        rv_recent_spectral_data_list.setAdapter(spectralDataListAdapter);
        scan.setEnabled(false);
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
        // 最后更新数据信息
        updateDeviceData();
        // 实现列表点击、长按监听器的接口
        spectralDataListAdapter.setOnItemClickListener(new RecentSpectralDataListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, List<String> dataList) {
                // TODO: 2024/8/19 当点击光谱时，存在索引文件但是本地不存在光谱，则弹窗提示并尝试删除本地光谱
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("userPhoneNumber", userPhoneNumber);
                        bundle.putString("fileName", dataList.get(position));

                        SpectralPreviewDialogFragment previewDialogFragment = SpectralPreviewDialogFragment.newInstance(
                                bundle
                        );
                        previewDialogFragment.show(getSupportFragmentManager(), "光谱预览");
                    }
                });
            }

            @Override
            public void OnItemLongClick(int position, List<String> dataList) {
                // 由于adapter管理的列表和实际列表为翻转关系，此处更新实际position
//                int itemPosition = SpectralDataUtils.userSpectralFileMap.size() - (position + 1);
                // 当长按列表选项时
                // TODO: 2024/8/18 显示菜单、提供删除选项
                String dateTime = Objects.requireNonNull(SpectralDataUtils.userSpectralFileMap.get(dataList.get(position))).getDateTime();
                // 创建对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("预测结果");
                builder.setMessage(dateTime + "\n是否删除本条结果？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fileName = dataList.get(position);
                        // 根据文件名删除索引
                        PredictionResultDescription remove = SpectralDataUtils.userSpectralFileMap.remove(fileName);
                        spectralDataListAdapter.updateDataList();

                        if (remove != null) {
                            // 更新索引到本地
                            // 删除本地光谱
                            if (!SpectralDataUtils.saveSpectrumFileMapToLocal(mContext, userPhoneNumber, deviceMac)
                                    | !SpectralDataUtils.deleteNirSpectralDataFile(mContext, userPhoneNumber, dataList.get(position))) {
                                Log.e(TAG, "onClick: 光谱索引或光谱文件删除失败！");
                            } else {
                                Log.d(TAG, "onClick: 光谱索引或光谱文件删除成功！");
                                spectralDataListAdapter.notifyItemRemoved(position);
                                spectralDataListAdapter.notifyItemRangeChanged(position, SpectralDataUtils.userSpectralFileMap.size() - position);
                                runOnUiThread(() -> Toast.makeText(mContext, dateTime + "删除成功！", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", (dialogInterface, i) -> {
                    // 不做处理，直接关闭弹窗
                    dialogInterface.dismiss();
                });
                builder.setCancelable(true);
                AlertDialog alertDialog = builder.create();
                // 设置自定义背景（圆角）
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                alertDialog.show();
            }
        });
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

    // todo:读取本地的光谱、光源通电时长、参比日期、光谱数量更新设备信息并更新到界面。
    @SuppressLint("NotifyDataSetChanged")
    private void updateDeviceData() {
        device_connection_layout.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startAnimation(fadeIn);
        connect_btn.setVisibility(View.GONE);
        connect_text.startAnimation(fadeOut);
        connect_text.setVisibility(View.GONE);
        if (deviceItemFromLocal != null) {
            tv_device_name.setText(deviceItemFromLocal.getDeviceName());
        }
        // 读取本地数据
        // 更新本地光谱数量
        numberOfSpectraCollected = SpectralDataUtils.userSpectralFileMap.size();
        battery = sharedPreferences.getInt(getString(R.string.pref_device_battery), -1);
        totalLampTime = sharedPreferences.getString(getString(R.string.pref_device_totalLampTime), "-");
        referenceUpdateDate = sharedPreferences.getString(getString(R.string.pref_app_reference_update_time), "-");
        light_usage_duration.setText(totalLampTime);
        spectral_reference_update_date_value.setText(referenceUpdateDate);
        number_of_spectra_collected_value.setText(String.valueOf(numberOfSpectraCollected));
        if (battery > 0) {
            battery_level.setText(getString(R.string.battery_level, String.valueOf(battery) + "%"));
        } else {
            battery_level.setText(getString(R.string.not_available));
        }

        battery_image.setImageResource(upDateBatteryIcon(battery));
        // 更新整个数据列表
        spectralDataListAdapter.updateDataList();
        spectralDataListAdapter.notifyDataSetChanged();

        if (numberOfSpectraCollected > 0) {
            rv_recent_spectral_data_list.setVisibility(View.VISIBLE);
            recent_spectral_data_list_empty.setVisibility(View.INVISIBLE);
        } else {
            rv_recent_spectral_data_list.setVisibility(View.INVISIBLE);
            recent_spectral_data_list_empty.setVisibility(View.VISIBLE);
        }
        // TODO: 2024/8/17 将此处的演示动画改为实际加载
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
                        scan.setEnabled(true);
                    }
                });
            }
        }, 1000L);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan) {
            // todo:将是否预热灯源、设备deviceItem实例，传输给ScanViewActivity。
            // 点击了扫描按钮
            Log.d("DeviceDetailsActivity", "点击了扫描按钮");
            // 开始进入扫描页面
            startScanPage();
            // 防止重复点击
            scan.setEnabled(false);
        } else if (view.getId() == R.id.device_connection_layout) {
            // 点击了设备连接布局
            Log.d("DeviceDetailsActivity", "点击了更新光谱数据");
            // 更新设备数据
            updateDeviceData();
            // 更新光谱数据
            spectralDataListAdapter.updateDataList();
            spectralDataListAdapter.notifyDataSetChanged();
        } else if (view.getId() == R.id.imageButton_back) {
            // 点击了返回按钮
            finish();
        } else if (view.getId() == R.id.imageButton_menu) {
            // 点击了菜单按钮
            Log.d("DeviceDetailsActivity", "点击了菜单按钮");
            /* TODO: 2024/8/1 下方弹出抽屉式视图（DialogFragment实现），功能列表有：
                 1.清除本地参比；
                 2.检索光谱；
                 3.设备详情（可以查看设备信息）；
                 4.连接设备；
            */
            imageButton_menu.setEnabled(false);
            DeviceDetailsMenuDialogFragment menuDialogFragment = getDeviceDetailsMenuDialogFragment();
            menuDialogFragment.show(getSupportFragmentManager(), "DeviceDetailsMenuDialogFragment");
        }
    }

    private void startScanPage() {
        Intent intent = new Intent(DeviceDetailsActivity.this, ScanViewActivity.class);
        intent.putExtra("deviceItem", deviceItem);
        intent.putExtra("warmUp", warmUp);
        intent.putExtra("mainFlag", true);
        startActivity(intent);
    }

    private @NonNull DeviceDetailsMenuDialogFragment getDeviceDetailsMenuDialogFragment() {
        DeviceDetailsMenuDialogFragment menuDialogFragment = new DeviceDetailsMenuDialogFragment(menuItems, deviceItem);
        menuDialogFragment.setOnMenuCloseListener(new DeviceDetailsMenuDialogFragment.OnMenuCloseListener() {
            @Override
            public void onClose() {
                imageButton_menu.setEnabled(true);
            }
        });
        menuDialogFragment.setActivityOnItemClickListener(new DeviceDetailsMenuDialogFragment.ActivityOnItemClickListener() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        modifyDeviceInformation();
                        break;
                    case 1:
                        deleteReferenceIntensityFile();
                        break;
                    case 2:
                        deleteDevice();
                        break;
                    case 3:
                        // TODO: 2024/8/19 增加检索光谱的功能。
                        break;
                    case 4:
                        startScanPage();
                        break;
                }
            }
        });
        return menuDialogFragment;
    }

    private void deleteReferenceIntensityFile() {
        File referenceFile = new File(getFilesDir(), "localReferenceIntensity.ser");
        boolean deleted = referenceFile.delete();
        if (deleted) {
            Toast.makeText(this, "本地参比已成功删除！", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.pref_app_reference_update_time));
            editor.apply();
            // 更新设备详情页
            updateDeviceData();
        } else {
            Toast.makeText(this, "本地参比删除失败！", Toast.LENGTH_LONG).show();
        }
    }

    private void modifyDeviceInformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改设备信息");
        View view = getLayoutInflater().inflate(R.layout.modify_device_information_dialog_layout, null);
        device_type_input = view.findViewById(R.id.device_type_input);
        device_info_input = view.findViewById(R.id.device_info_input);
        device_name_input = view.findViewById(R.id.device_name_input);
        device_name_input.setText(deviceItem.getDeviceName());
        device_type_input.setText(deviceItem.getDeviceType());
        device_info_input.setText(deviceItem.getDeviceMac());
        // 注意修改信息时，应该使用列表中的对象
        if (devicePosition != -1) {
            deviceItemFromLocal = itemList.get(devicePosition);
        }

        builder.setView(view);

        builder.setPositiveButton("确认", (dialog, which) -> {
            if (device_name_input.getText().toString().isEmpty() || device_name_input.getText().toString().length() > 20) {
                Toast.makeText(this, "设备名称格式有误!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (deviceItemFromLocal != null) {
                deviceItemFromLocal.setDeviceName(device_name_input.getText().toString());
                Toast.makeText(this, "修改成功" + devicePosition, Toast.LENGTH_SHORT).show();
                // 将修改后的数据写入文件
                SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
                // 更新设备详情页
                updateDeviceData();
            } else {
                Toast.makeText(this, "修改失败" + devicePosition, Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNeutralButton("删除设备", (dialog, which) -> {
            if (devicePosition != -1) {
                itemList.remove(devicePosition);
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                // 更新数据并保存到文件
                SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
                // 更新设备详情页
                updateDeviceData();
                finish();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        // 创建AlertDialog
        AlertDialog dialog = builder.create();
        // 设置自定义背景（圆角）
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_rectangle);
        // 显示AlertDialog
        dialog.show();
    }

    private void deleteDevice() {
        if (devicePosition != -1) {
            itemList.remove(devicePosition);
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            // 更新数据并保存到文件
            SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
            // 更新设备详情页
            updateDeviceData();
            finish();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
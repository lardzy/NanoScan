package com.gttcgf.nanoscan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeviceListActivity extends AppCompatActivity {
    private List<DeviceItem> itemList;
    private Button button_add_device;
    private ImageButton imageButton_back;
    private View.OnClickListener onClickListener;
    private TextView device_type_input, device_info_input, device_list_empty;
    private EditText device_name_input;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DeviceListAdapter adapter;
    private static final String TAG = "DeviceListActivity";
    private SharedPreferences sharedPreferences;
    private String userPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.device_list), (v, insets) -> {
            Insets insets1 = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets1.left, insets1.top, insets1.right, insets1.bottom);
            return insets;
        });
        initializeData();
        initialComponent();

        // 注册activityResultLauncher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    Intent data = o.getData();
                    if (data != null) {
                        String deviceName = data.getStringExtra("NAME");
                        String deviceMac = data.getStringExtra("MAC");
                        String deviceToken = data.getStringExtra("DEVICE_TOKEN");
                        // 将选中的设备添加进集合
                        if (userPhoneNumber.isEmpty()) {
                            Log.e(TAG, "设备列表界面-未读取到用户手机号，设备添加失败！");
                            return;
                        }
                        addDevice(new DeviceItem(userPhoneNumber, R.drawable.equipment_front, deviceName, "便携式近红外光谱仪", deviceMac, deviceToken));
                    }
                }
            }
        });
    }

    private void initializeData() {
        sharedPreferences = this.getSharedPreferences("default", Context.MODE_PRIVATE);
        userPhoneNumber = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        itemList = new ArrayList<>();
        // 获取本地的序列化数据
        itemList = SpectralDataUtils.readDeviceListFromFile(this, userPhoneNumber);
    }

    private void initialComponent() {
        RecyclerView recyclerView = findViewById(R.id.device_list);
        button_add_device = findViewById(R.id.button_add_device);
        imageButton_back = findViewById(R.id.imageButton_back);
        device_list_empty = findViewById(R.id.device_list_empty);

        // 是否显示“列表为空
        updateEmptyState();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceListAdapter(itemList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                // 点击设备列表中的设备，跳转到设备详情页面
                Intent i = new Intent(DeviceListActivity.this, DeviceDetailsActivity.class);
                i.putExtra("deviceItem", itemList.get(position));
                startActivity(i);
            }

            @Override
            public void OnItemLongClick(int position) {
                modifyDeviceInformation(position, adapter);
            }
        });

        onClickListener = view -> {
            if (view.getId() == R.id.button_add_device) {
                // 添加新设备
                Intent intent = new Intent(DeviceListActivity.this, SelectDeviceViewActivity.class);
                activityResultLauncher.launch(intent);

            } else if (view.getId() == R.id.imageButton_back) {
                finish();
            }
        };
        // 绑定监听器
        button_add_device.setOnClickListener(onClickListener);
        imageButton_back.setOnClickListener(onClickListener);

    }

    // 用于长按列表项目条修改项目条内容。
    private void modifyDeviceInformation(int position, DeviceListAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
        builder.setTitle("修改设备信息");
        View view = getLayoutInflater().inflate(R.layout.modify_device_information_dialog_layout, null);
        device_type_input = view.findViewById(R.id.device_type_input);
        device_info_input = view.findViewById(R.id.device_info_input);
        device_name_input = view.findViewById(R.id.device_name_input);
        device_name_input.setText(itemList.get(position).getDeviceName());
        device_type_input.setText(itemList.get(position).getDeviceType());
        device_info_input.setText(itemList.get(position).getDeviceMac());

        builder.setView(view);

        builder.setPositiveButton("确认", (dialog, which) -> {
            if (device_name_input.getText().toString().isEmpty() || device_name_input.getText().toString().length() > 20) {
                Toast.makeText(DeviceListActivity.this, "设备名称格式有误!", Toast.LENGTH_SHORT).show();
                return;
            }
            itemList.get(position).setDeviceName(device_name_input.getText().toString());
            adapter.notifyItemChanged(position);
            Toast.makeText(DeviceListActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            // 将修改后的数据写入文件
            SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
        });
        builder.setNeutralButton("删除设备", (dialog, which) -> {
            itemList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, itemList.size() - position);
            Toast.makeText(DeviceListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            // 更新数据并保存到文件
            SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
            updateEmptyState();
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


    // 添加设备，更新列表和本地配置文件
    private void addDevice(DeviceItem newItem) {
        // TODO: 2024/8/5 这里需要调整，由于校验设备已经刷新了设备token，此处就算设备已存在，也要更新信息
        // 检查列表中是否有重复设备，使用mac地址校验。
        for (DeviceItem deviceItem : itemList) {
            if (deviceItem.getDeviceMac().equals(newItem.getDeviceMac())) {
                Toast.makeText(this, "设备已在列表中！", Toast.LENGTH_SHORT).show();
                // 仅更新设备token
                deviceItem.setDeviceToken(newItem.getDeviceToken());
                SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
                return;
            }
        }
        itemList.add(newItem);
        adapter.notifyItemInserted(itemList.size() - 1);
        updateEmptyState();
        SpectralDataUtils.writeDeviceListToFile(this, userPhoneNumber, itemList);
    }

    // 更新列表是否为空的状态
    private void updateEmptyState() {
        if (itemList.isEmpty()) {
            device_list_empty.setVisibility(TextView.VISIBLE);
        } else {
            device_list_empty.setVisibility(TextView.INVISIBLE);
        }
    }

}
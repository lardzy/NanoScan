package com.gttcgf.nanoscan;

import android.app.Activity;
import android.content.Intent;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {
    private  List<DeviceItem> itemList;
    private Button button_add_device;
    private ImageButton imageButton_search, imageButton_back;
    private View.OnClickListener onClickListener;
    private TextView device_type_input, device_info_input, device_list_empty;
    private EditText device_name_input;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DeviceListAdapter adapter;
    private static final String TAG = "DeviceListActivity";

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
                        addDevice(new DeviceItem(R.drawable.equipment_front, deviceName, "便携式近红外光谱仪", deviceMac, deviceToken));
                    }
                }
            }
        });
    }

    private void initializeData() {
        itemList = new ArrayList<>();
        // 获取本地的序列化数据
        loadItemListFromFile();
//        itemList.add(new DeviceItem(R.drawable.equipment_front, "演示设备1", "便携式近红外光谱仪"));
    }

    private void initialComponent() {
        RecyclerView recyclerView = findViewById(R.id.device_list);
        button_add_device = findViewById(R.id.button_add_device);
        imageButton_search = findViewById(R.id.imageButton_search);
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
//                startActivity(intent);
                activityResultLauncher.launch(intent);

            } else if (view.getId() == R.id.imageButton_back) {
                finish();
            } else if (view.getId() == R.id.imageButton_search) {
                Toast.makeText(DeviceListActivity.this, "搜索设备功能正在开发中...", Toast.LENGTH_SHORT).show();
            }
        };
        // 绑定监听器
        button_add_device.setOnClickListener(onClickListener);
        imageButton_search.setOnClickListener(onClickListener);
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

            itemListChangedToFile();
        });
        builder.setNeutralButton("删除设备", (dialog, which) -> {
            itemList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, itemList.size() - position);
            Toast.makeText(DeviceListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            // 更新
            itemListChangedToFile();
            updateEmptyState();
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        // 创建并显示AlertDialog
        builder.create().show();
    }

    // 添加设备，更新列表和本地配置文件
    private void addDevice(DeviceItem newItem) {
        for (DeviceItem deviceItem : itemList) {
            if (deviceItem.getDeviceMac().equals(newItem.getDeviceMac())) {
                Toast.makeText(this, "设备已在列表中！", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        itemList.add(newItem);
        adapter.notifyItemInserted(itemList.size() - 1);
        updateEmptyState();
        itemListChangedToFile();
    }

    // 更新列表是否为空的状态
    private void updateEmptyState() {
        if (itemList.isEmpty()) {
            device_list_empty.setVisibility(TextView.VISIBLE);
        } else {
            device_list_empty.setVisibility(TextView.INVISIBLE);
        }
    }

    private void itemListChangedToFile() {
        try (FileOutputStream fos = openFileOutput("deviceItem.ser", MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(itemList);
            Log.d(TAG, "设备列表配置文件已写入本地");
        } catch (IOException e) {
            Toast.makeText(this, "配置信息写入失败，请检查设备存储空间！", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItemListFromFile() {
        try (FileInputStream fis = openFileInput("deviceItem.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            itemList = (List<DeviceItem>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "设备列表文件读取失败");
        }
    }
}
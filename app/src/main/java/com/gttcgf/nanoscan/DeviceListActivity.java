package com.gttcgf.nanoscan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {
    private List<DeviceItem> itemList;
    private Button button_add_device;
    private ImageButton imageButton_back, imageButton_search;
    private View.OnClickListener onClickListener;
    private TextView device_type_input, device_info_input;
    private EditText device_name_input;
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
    }
    private void initializeData(){
        itemList = new ArrayList<>();
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备1", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备2", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备3", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备4", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备5", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备6", "便携式近红外光谱仪"));
    }
    private void initialComponent(){
        RecyclerView recyclerView = findViewById(R.id.device_list);
        button_add_device = findViewById(R.id.button_add_device);
        imageButton_back = findViewById(R.id.imageButton_back);
        imageButton_search = findViewById(R.id.imageButton_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DeviceListAdapter adapter = new DeviceListAdapter(itemList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Toast.makeText(DeviceListActivity.this,position + " ," +"点击了：" +
                        itemList.get(position).getDeviceName(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnItemLongClick(int position) {
                modifyDeviceInformation(position, adapter);
            }
        });

        onClickListener = view -> {
            if (view.getId() == R.id.button_add_device){
                Toast.makeText(DeviceListActivity.this,"点击了添加设备",Toast.LENGTH_SHORT).show();
            } else if (view.getId() == R.id.imageButton_back){
                finish();
            } else if (view.getId() == R.id.imageButton_search){
                Toast.makeText(DeviceListActivity.this,"点击了搜索设备",Toast.LENGTH_SHORT).show();
            }
        };
        // 绑定监听器
        button_add_device.setOnClickListener(onClickListener);
        imageButton_back.setOnClickListener(onClickListener);
        imageButton_search.setOnClickListener(onClickListener);
    }
    // todo:这个方法仅测试用，正式版将删除。
    private void modifyDeviceInformation(int position, DeviceListAdapter adapter){
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
        builder.setTitle("修改设备信息");
        View view = getLayoutInflater().inflate(R.layout.modify_device_information_dialog_layout, null);
        device_type_input = view.findViewById(R.id.device_type_input);
        device_info_input = view.findViewById(R.id.device_info_input);
        device_name_input = view.findViewById(R.id.device_name_input);
        device_name_input.setText(itemList.get(position).getDeviceName());
        device_type_input.setText(itemList.get(position).getDeviceType());

        builder.setView(view);

        builder.setPositiveButton("确认", (dialog, which) -> {
            if (device_name_input.getText().toString().isEmpty() || device_name_input.getText().toString().length() > 10){
                Toast.makeText(DeviceListActivity.this,"设备名称格式有误!",Toast.LENGTH_SHORT).show();
                return;
            }
            itemList.get(position).setDeviceName(device_name_input.getText().toString());
            adapter.notifyItemChanged(position);
            Toast.makeText(DeviceListActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
        });
        builder.setNeutralButton("删除设备", (dialog, which) -> {
            itemList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, itemList.size() - position);
            Toast.makeText(DeviceListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        // 创建并显示AlertDialog
        builder.create().show();
    }
}
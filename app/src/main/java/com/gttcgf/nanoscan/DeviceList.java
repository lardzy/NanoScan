package com.gttcgf.nanoscan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends AppCompatActivity {
    private List<DeviceItem> itemList;
    private Button button_add_device;
    private ImageButton imageButton_back, imageButton_search;
    private View.OnClickListener onClickListener;
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

        adapter.setOnItemClickListener(position -> {
            Toast.makeText(this,"点击了：" +
                    itemList.get(position).getDeviceName(),Toast.LENGTH_SHORT).show();
        });

        onClickListener = view -> {
            if (view.getId() == R.id.button_add_device){
                Toast.makeText(DeviceList.this,"点击了添加设备",Toast.LENGTH_SHORT).show();
            } else if (view.getId() == R.id.imageButton_back){
                finish();
            } else if (view.getId() == R.id.imageButton_search){
                Toast.makeText(DeviceList.this,"点击了搜索设备",Toast.LENGTH_SHORT).show();
            }
        };

        button_add_device.setOnClickListener(onClickListener);
        imageButton_back.setOnClickListener(onClickListener);
        imageButton_search.setOnClickListener(onClickListener);
    }
}
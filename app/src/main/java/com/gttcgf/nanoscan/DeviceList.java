package com.gttcgf.nanoscan;

import android.os.Bundle;

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
        List<DeviceItem> itemList = new ArrayList<>();
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备1", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备2", "便携式近红外光谱仪"));
        itemList.add(new DeviceItem(R.drawable.equipment_front, "设备3", "便携式近红外光谱仪"));
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.device_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DeviceListAdapter adapter = new DeviceListAdapter(itemList); // itemList 是你的数据集
        recyclerView.setAdapter(adapter);

    }
}
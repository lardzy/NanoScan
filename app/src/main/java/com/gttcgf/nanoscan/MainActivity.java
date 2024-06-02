package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton ib_shutdown, ib_account, ib_add_device;
    private EditText et_search;
    private RecyclerView rv_devices_list;
    private ProgressBar pb_devices_list, pb_news;
    private TextView tv_devices_list_empty, tv_nes_empty;
    private List<DeviceItem> deviceItem;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        initializeData();
        initComponent();

        // 应用窗口边缘到边缘的设置
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkFirstRunOrUserAgreement();
    }

    private void initializeData() {
        deviceItem = new ArrayList<>();
        loadDataFromFiles();
        // todo:增加获取消息列表的功能
    }

    private void loadDataFromFiles() {
        try (FileInputStream fis = openFileInput("deviceItem.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            deviceItem = (List<DeviceItem>) ois.readObject();
            Log.d(TAG, "主界面-设备列表文件读取成功！列表长度：" + deviceItem.size());
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "主界面-设备列表文件不存在或读取失败！");
        }
    }

    private void initComponent() {
        ib_shutdown = findViewById(R.id.ib_shutdown);
        ib_account = findViewById(R.id.ib_account);
        ib_add_device = findViewById(R.id.ib_add_device);
        et_search = findViewById(R.id.et_search);
        rv_devices_list = findViewById(R.id.rv_devices_list);
        pb_devices_list = findViewById(R.id.pb_devices_list);
        pb_news = findViewById(R.id.pb_news);
        tv_devices_list_empty = findViewById(R.id.tv_devices_list_empty);
        tv_nes_empty = findViewById(R.id.tv_nes_empty);

        et_search.setEnabled(false);
        ib_shutdown.setOnClickListener(this);
        ib_add_device.setOnClickListener(this);

        if (deviceItem.isEmpty()) {
            tv_nes_empty.setVisibility(View.VISIBLE);
        }
        pb_devices_list.setVisibility(View.INVISIBLE);
        pb_news.setVisibility(View.INVISIBLE);
        tv_nes_empty.setVisibility(View.VISIBLE);
        rv_devices_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        MainActivityDeviceListAdapter deviceListAdapter = new MainActivityDeviceListAdapter(deviceItem);
        rv_devices_list.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener(new MainActivityDeviceListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                // 点击设备列表中的设备，跳转到设备详情页面
                Intent i = new Intent(MainActivity.this, DeviceDetailsActivity.class);
                i.putExtra("deviceItem", deviceItem.get(position));
                startActivity(i);
            }
        });
    }

    private void checkFirstRunOrUserAgreement() {
        Log.d(TAG, "主界面-checkFirstRunOrUserAgreement called");

        SharedPreferences prefs = this.getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(getString(R.string.pref_first_run), true);
        boolean userAgreed = prefs.getBoolean(getString(R.string.pref_user_agreed), false);

        Intent intent;
        if (isFirstRun && !userAgreed) {
            // UserAgreementActivity 是用户协议界面的Activity
            Log.d(TAG, "主界面-用户是第一次使用，并未同意用户协议！");
            intent = new Intent(this, UserAgreementActivity.class);
            startActivity(intent);
            finish(); // 关闭当前活动，以防用户返回到这个界面
        } else if (!LoginActivity.userLoggedIn) {
            // LoginActivity 是登录界面的Activity
            // todo:增加判断是否登录的逻辑。
            Log.d(TAG, "主界面-用户不是第一次使用，已同意用户协议，但是登录TOKEN不存在或过期！");
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // 关闭当前活动，以防用户返回到这个界面
        } else {
            Log.d(TAG, "主界面-用户已登录！");
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_shutdown) {
            finish();
        } else if (view.getId() == R.id.ib_add_device) {
            Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivity(i);
        }
    }
}
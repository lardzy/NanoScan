package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private String userAccount;
    private TextView tv_account;
    private RecyclerView rv_functions;
    private SharedPreferences sharedPreferences;
    private List<UserProfileFunctionItem> functionItems = new ArrayList<>();
    private UserProfileFunctionAdapter adapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        mContext = getApplicationContext();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialData();
        initialComponent();
    }

    private void initialData() {
        sharedPreferences = getSharedPreferences("default", MODE_PRIVATE);
        userAccount = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");

        functionItems.add(new UserProfileFunctionItem(R.drawable.baseline_settings_applications_24, getString(R.string.user_profile_item_device_manage)));
        functionItems.add(new UserProfileFunctionItem(R.drawable.baseline_info_outline_24, getString(R.string.user_profile_item_about)));
        functionItems.add(new UserProfileFunctionItem(R.drawable.baseline_logout_24, getString(R.string.user_profile_item_logout)));
    }

    private void initialComponent() {
        tv_account = findViewById(R.id.tv_account);
        rv_functions = findViewById(R.id.rv_functions);

        tv_account.setText(getString(R.string.user_profile_account, userAccount));
        rv_functions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new UserProfileFunctionAdapter(functionItems, new UserProfileFunctionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        // 进入设备管理界面
                        Intent i = new Intent(UserProfileActivity.this, DeviceListActivity.class);
                        startActivity(i);
                        break;
                    case 1:
                        // 打开关于界面
                        Toast.makeText(mContext, "-广检集团材料检测中心开发-", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        // 执行登出逻辑
                        LoginActivity.userLoggedIn = false;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.pref_user_token), "");
                        editor.putString(getString(R.string.pref_user_password), "");
                        editor.apply();
                        finish(); // 关闭当前活动，以防用户返回到这个界面
                        break;
                }
            }
        });
        rv_functions.setAdapter(adapter);

    }
}
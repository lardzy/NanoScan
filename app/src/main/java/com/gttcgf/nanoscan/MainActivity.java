package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // 应用窗口边缘到边缘的设置
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkFirstRunOrUserAgreement();
    }

    private void checkFirstRunOrUserAgreement() {
        Log.d("MainActivity", "checkFirstRunOrUserAgreement called");

        SharedPreferences prefs = this.getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(getString(R.string.pref_first_run), true);
        boolean userAgreed = prefs.getBoolean(getString(R.string.pref_user_agreed), false);

        Intent intent;// 同样关闭当前活动
        if (isFirstRun || !userAgreed) {
            // UserAgreementActivity 是用户协议界面的Activity
            intent = new Intent(this, UserAgreementActivity.class);
        } else {
            // LoginActivity 是登录界面的Activity
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish(); // 关闭当前活动，以防用户返回到这个界面
    }
}
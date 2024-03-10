package com.gttcgf.nanoscan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserAgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_agreement);

        Button btnAccept = findViewById(R.id.btn_accept);
        Button btnReject = findViewById(R.id.btn_reject);
        TextView tvUserAgreement = findViewById(R.id.tv_user_agreement);
        String userAgreementContent = getString(R.string.user_agreement_content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvUserAgreement.setText(Html.fromHtml(userAgreementContent, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvUserAgreement.setText(Html.fromHtml(userAgreementContent));
        }
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 用户接受协议
                userAcceptedAgreement();
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 用户拒绝协议，可执行退出应用或返回上一页等操作
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void userAcceptedAgreement() {
        SharedPreferences prefs = this.getSharedPreferences("default", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(getString(R.string.pref_first_run), false).apply();
        prefs.edit().putBoolean(getString(R.string.pref_user_agreed), true).apply();

        // 用户接受协议后跳转到登录界面
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // 关闭当前活动，防止用户返回到用户协议界面
    }
}
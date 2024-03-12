package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerification;
import com.gttcgf.nanoscan.tools.PortraitCaptureActivity;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText phone_number, sms_verification_code, password, check_code;
    private Button register_button;
    private ImageButton imageButton_back;
    private TextView get_verification_code, register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        initialComponent();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 将扫描授权码赋值到文本框。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "扫描取消", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(this, "扫描结果: " + result.getContents(), Toast.LENGTH_LONG).show();
                check_code.setText(result.getContents());
                check_code.setSelection(check_code.getText().length());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent(){
        phone_number = findViewById(R.id.phone_number);
        sms_verification_code = findViewById(R.id.sms_verification_code);
        password = findViewById(R.id.password);
        check_code = findViewById(R.id.check_code);
        register_button = findViewById(R.id.register_button);
        imageButton_back = findViewById(R.id.imageButton_back);
        get_verification_code = findViewById(R.id.get_verification_code);
        register = findViewById(R.id.register);

        // 设置手机号输入框的输入限制
        phone_number.addTextChangedListener(new CustomTextWatcher(phone_number,
                InputDataVerification::phoneNumberInputVerification, "请输入正确的手机号", 11, () -> sms_verification_code.requestFocus()));
        // 设置短信验证码输入框的输入限制
        sms_verification_code.addTextChangedListener(new CustomTextWatcher(sms_verification_code,
                InputDataVerification::smsVerificationCodeVerification, "请输入正确的验证码"));
        // 设置密码输入框的输入限制
        password.addTextChangedListener(new CustomTextWatcher(password,
                InputDataVerification::passwordInputVerification, "密码至少包含8个字符，可包含数字、大小写字母和符号"));
        check_code.addTextChangedListener(new CustomTextWatcher(check_code,
                InputDataVerification::checkCodeVerification, "请输入正确的授权码，授权码位于机身正面的机身按钮下侧"));
        // 扫描授权码
        check_code.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (check_code.getRight() - check_code.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - check_code.getCompoundDrawablePadding())) {
                    // 扫描条形码
//                    Toast.makeText(RegisterActivity.this, "drawableRight被点击了！", Toast.LENGTH_SHORT).show();
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setOrientationLocked(true); // 锁定屏幕方向
                    integrator.setCaptureActivity(PortraitCaptureActivity.class); // 使用自定义活动
                    integrator.initiateScan();
                    return true;
                }
            }
            return false;
        });
        imageButton_back.setOnClickListener(v -> finish());
        // 注册按钮的点击事件，如果注册界面已经打开，则返回，否则重新打开注册界面。
        register.setOnClickListener(v -> {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (ActivityManager.RunningTaskInfo task : tasks) {
                if ("com.gttcgf.nanoscan.LoginActivity".equals(task.baseActivity.getClassName())) {
                    finish();
                    return;
                }
            }
            // 如果注册界面没有打开，则重新打开注册界面。
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        // 获取验证码按钮的点击事件
        get_verification_code.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "点击获取注册码", Toast.LENGTH_SHORT).show();
        });
        // 点击注册码按钮的事件
        register_button.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "点击注册！", Toast.LENGTH_SHORT).show();
        });
        // 设置密码输入框的显示状态
        password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - password.getPaddingEnd())) {
                    // 切换密码的显示状态
                    if (password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    // 将光标移动到文本末尾
                    password.setSelection(password.getText().length());
                    v.performClick(); // 添加这行代码
                    return true;
                }
            }
            return false;
        });

    }
}
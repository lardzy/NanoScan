package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.DialogFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerificationUtils;
import com.gttcgf.nanoscan.tools.PasswordUtils;
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
                check_code.setText(result.getContents());
                check_code.setSelection(check_code.getText().length());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent() {
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
                InputDataVerificationUtils::phoneNumberInputVerification, "请输入正确的手机号", 11, () -> sms_verification_code.requestFocus()));
        // 设置短信验证码输入框的输入限制
        sms_verification_code.addTextChangedListener(new CustomTextWatcher(sms_verification_code,
                InputDataVerificationUtils::smsVerificationCodeVerification, "请输入正确的验证码", false));
        // 设置密码输入框的输入限制
        password.addTextChangedListener(new CustomTextWatcher(password,
                InputDataVerificationUtils::passwordInputVerification, "密码至少包含8个字符，可包含数字、大小写字母和符号", false));
        check_code.addTextChangedListener(new CustomTextWatcher(check_code,
                InputDataVerificationUtils::checkCodeVerification, "请输入正确的授权码，授权码位于机身正面的机身按钮下侧"));
        // 扫描授权码
        check_code.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (check_code.getRight() - check_code.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - check_code.getCompoundDrawablePadding())) {
                    // 扫描条形码
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
        // 登录按钮的点击事件，如果登录界面已经打开，则返回，否则重新打开登录界面。
        register.setOnClickListener(v -> {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (ActivityManager.RunningTaskInfo task : tasks) {
                if ("com.gttcgf.nanoscan.LoginActivity".equals(task.baseActivity.getClassName())) {
                    finish();
                    return;
                }
            }
            // 如果登录界面没有打开，则重新打开登录界面。
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        // 获取验证码按钮的点击事件
        get_verification_code.setOnClickListener(v -> {
            DialogFragment df = VerificationCodeDialogFragment.newInstance("test");
            df.show(getSupportFragmentManager(), "VerificationCodeDialogFragment");
        });

        // 点击立即注册按钮的事件
        register_button.setOnClickListener(v -> {
            if (checkComponents() && serverVerification()) {
                // 注册成功后，保存用户名、加密并保存密码
                SharedPreferences sharedPreferences = this.getSharedPreferences("default", Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(getString(R.string.pref_user_phone_number), phone_number.getText().toString()).apply();
                String passwordHash = PasswordUtils.hashPassword(password.getText().toString());
                sharedPreferences.edit().putString(getString(R.string.pref_user_password), passwordHash).apply();
                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                finish();
            }
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

    // 检查输入内容是否合法，如果合法则继续注册，否则提示错误。
    private boolean checkComponents() {
        String phoneNumber = phone_number.getText().toString();
        String smsCode = sms_verification_code.getText().toString();
        String password = this.password.getText().toString();
        String checkCode = check_code.getText().toString();
        if (!InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber)) {
            phone_number.setError("请输入正确的手机号");
            Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.smsVerificationCodeVerification(smsCode)) {
            sms_verification_code.setError("请输入正确的验证码", null);
            Toast.makeText(RegisterActivity.this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.passwordInputVerification(password)) {
            this.password.setError("密码至少包含8个字符，可包含数字、大小写字母和符号", null);
            Toast.makeText(RegisterActivity.this, "密码至少包含8个字符，可包含数字、大小写字母和符号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerificationUtils.checkCodeVerification(checkCode)) {
            check_code.setError("请输入正确的授权码，授权码位于机身正面的机身按钮下侧");
            Toast.makeText(RegisterActivity.this, "请输入正确的授权码，授权码位于机身正面的机身按钮下侧", Toast.LENGTH_SHORT).show();
            return false;
        } else if (InputDataVerificationUtils.phoneNumberInputVerification(phoneNumber)
                && InputDataVerificationUtils.smsVerificationCodeVerification(smsCode) &&
                InputDataVerificationUtils.passwordInputVerification(password) &&
                InputDataVerificationUtils.checkCodeVerification(checkCode)) {
            // 注册
            return true;
        }
        return false;
    }

    // 将信息发送至服务器进行验证。
    private boolean serverVerification() {
        return true;
    }
//    private boolean updateDatabase(){
//        DatabaseUtils databaseUtils = new DatabaseUtils(this);
//        ContentValues values = new ContentValues();
//
//        String passwordHash = PasswordUtils.hashPassword(this.password.getText().toString());
//
//        values.put("PhoneNumber", phone_number.getText().toString());
//        values.put("PasswordHash", passwordHash);
//        values.put("LoginToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
//                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
//                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
//        values.put("CreateTime", DatabaseUtils.getCurrentTime());
//        values.put("UpdateTime", DatabaseUtils.getCurrentTime());
//
//        return databaseUtils.insertData(values);
//    }
}
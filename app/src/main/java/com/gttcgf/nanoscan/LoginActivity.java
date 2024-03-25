package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gttcgf.nanoscan.guidingSteps.IntroGuideActivity;
import com.gttcgf.nanoscan.tools.CustomTextWatcher;
import com.gttcgf.nanoscan.tools.InputDataVerification;

public class LoginActivity extends AppCompatActivity {
    private EditText phone_number, password;
    private TextView forgot_password, register;
    private Button login_button;
    private boolean userHasEditedPassword;
    private String pref_user_phone_number, pref_user_password;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 初始化组件
        initialComponent();
        // 初始化用户数据
        initializeData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("LoginActivity", "onRestart");
        // 初始化用户数据
        initializeData();
    }

    // 初始化组件
    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent() {
        setContentView(R.layout.activity_login);
        phone_number = findViewById(R.id.phone_number); // 获取手机号输入框
        password = findViewById(R.id.password); // 获取密码输入框
        forgot_password = findViewById(R.id.forgot_password); // 获取忘记密码按钮
        register = findViewById(R.id.register); // 获取注册按钮
        login_button = findViewById(R.id.login_button);

        // 设置手机号输入框的输入限制
        phone_number.addTextChangedListener(new CustomTextWatcher(phone_number,
                InputDataVerification::phoneNumberInputVerification, "请输入正确的手机号", 11, () -> password.requestFocus()));

        // 设置密码输入框的输入限制
        password.addTextChangedListener(new CustomTextWatcher(password,
                InputDataVerification::passwordInputVerification, "密码至少包含8个字符，可包含数字、大小写字母和符号", false));

        // 设置密码输入框的监听事件, 当用户编辑自动输入的密码时，清空本地存储的密码
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // 检查用户是否编辑过密码
                if (!userHasEditedPassword) {
                    // 清空密码
                    password.getText().clear();
                    userHasEditedPassword = true;
                    clearPassword();
                }
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
                        // 当密码为自动填入时，用户尝试查看密码，清空密码。
                        if (!userHasEditedPassword){
                            password.getText().clear();
                            userHasEditedPassword = true;
                            clearPassword();
                        }
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

        // 设置忘记密码按钮的点击事件
        forgot_password.setOnClickListener(view ->
                Toast.makeText(LoginActivity.this, "点击了忘记密码", Toast.LENGTH_SHORT).show());
        // 设置注册按钮的点击事件
        register.setOnClickListener(view -> {
            // 跳转到注册页面
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);

        });
        login_button.setOnClickListener(view -> {
            // 登录
            if (checkLogin()){
                // 登录成功，跳转到主页面
                Intent i = new Intent(LoginActivity.this, IntroGuideActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    // 检查输入框信息。
    private boolean checkLogin(){
        String phoneNumber = phone_number.getText().toString();
        String password = this.password.getText().toString();
        if (!InputDataVerification.phoneNumberInputVerification(phoneNumber)){
            phone_number.setError("请输入正确的手机号");
            Toast.makeText(LoginActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!InputDataVerification.passwordInputVerification(password)) {
            this.password.setError("密码至少包含8个字符，可包含数字、大小写字母和符号",null);
            Toast.makeText(LoginActivity.this, "密码至少包含8个字符，可包含数字、大小写字母和符号", Toast.LENGTH_SHORT).show();
            return false;
        } else if (InputDataVerification.phoneNumberInputVerification(phoneNumber) && InputDataVerification.passwordInputVerification(password)){
            // 登录
//            Toast.makeText(LoginActivity.this, "登录", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    // 初始化用户数据
    private void initializeData(){

        userHasEditedPassword = true;

        sharedPreferences = this.getSharedPreferences("default", MODE_PRIVATE);
        pref_user_phone_number = sharedPreferences.getString(getString(R.string.pref_user_phone_number), "");
        pref_user_password = sharedPreferences.getString(getString(R.string.pref_user_password), "");

        if (pref_user_phone_number.isEmpty() || pref_user_password.isEmpty()){
            return;
        }

        phone_number.setText(pref_user_phone_number);
        password.setText(pref_user_password);

        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);

        userHasEditedPassword = false;
    }

    // 当用户编辑密码框时，清空本地存储的密码
    private void clearPassword(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_user_password), "");
        editor.apply();
    }
}
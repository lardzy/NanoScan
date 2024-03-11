package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.internal.TextWatcherAdapter;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText phone_number, password;
    private TextView forgot_password, register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        phone_number = findViewById(R.id.phone_number); // 获取手机号输入框
        password = findViewById(R.id.password); // 获取密码输入框
        forgot_password = findViewById(R.id.forgot_password); // 获取忘记密码按钮
        register = findViewById(R.id.register); // 获取注册按钮

        initialComponent();
        // 初始化密码输入框的显示状态


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 初始化组件
    @SuppressLint("ClickableViewAccessibility")
    private void initialComponent() {
        // 设置手机号输入框的输入限制
        phone_number.addTextChangedListener(new TextWatcher() {
               @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // 在文本变化之前执行
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 在文本变化时执行
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // 在文本变化之后执行
                    if (s.length() == 0) {
                        return;
                    }else {
//                        validateInput(s, phone_number);
                        String phoneRegex = "1[3-9]\\d{9}";
                        if (!s.toString().matches(phoneRegex)) {
                            phone_number.setError("请输入正确的手机号");
                        } else if (s.length() == 11){
                            phone_number.setError(null);
                            password.requestFocus();
                        }
                    }
                }

        });
        // 设置密码输入框的输入限制
        password.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
               String regex = "[\\x21-\\x7E]{8,32}";
               Pattern p = Pattern.compile(regex);
                if (!p.matcher(editable).matches()) {
                     password.setError("密码至少包含8个字符，可包含数字、大小写字母和符号", null);
                } else {
                     password.setError(null);
                }
            }
        });
        // 设置密码输入框的显示状态
        password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - password.getPaddingEnd())) {
                    // 切换密码的显示状态
                    if (password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_black_24dp, 0);
                    } else {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_black_24dp, 0);
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
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "点击了忘记密码", Toast.LENGTH_SHORT).show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到注册页面
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);

            }
        });

    }

}
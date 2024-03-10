package com.gttcgf.nanoscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {
    private Animation topAnim, bottomAnim;
    private ImageView image;
    private TextView slogan, app_version_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.buttom_animation);

        image = findViewById(R.id.shape);
        slogan = findViewById(R.id.app_name);
        app_version_name = findViewById(R.id.app_version_name);

        image.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
        app_version_name.setAnimation(bottomAnim);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }, 3000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
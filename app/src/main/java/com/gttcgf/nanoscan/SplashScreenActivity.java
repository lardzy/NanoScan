package com.gttcgf.nanoscan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    private Animation topAnim, bottomAnim;
    private ImageView image;
    private TextView slogan, app_version_name;
    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Called");
        setContentView(R.layout.activity_splash_screen);


        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.buttom_animation);

        image = findViewById(R.id.shape);
        slogan = findViewById(R.id.app_name);
        app_version_name = findViewById(R.id.app_version_name);

        image.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);
        app_version_name.setAnimation(bottomAnim);

        bottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy Called");
    }
}


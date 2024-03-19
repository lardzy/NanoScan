package com.gttcgf.nanoscan;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class IntroGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_guide);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        GuideStepAdapter adapter = new GuideStepAdapter(this);
        viewPager.setAdapter(adapter);

    }
}
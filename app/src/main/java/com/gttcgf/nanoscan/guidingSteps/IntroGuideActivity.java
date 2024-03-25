package com.gttcgf.nanoscan.guidingSteps;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.gttcgf.nanoscan.R;

public class IntroGuideActivity extends AppCompatActivity {

    private ImageButton next_step;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_guide);

        ViewPager2 viewPager = findViewById(R.id.viewPager);

        GuideStepAdapter adapter = new GuideStepAdapter(this);
        viewPager.setAdapter(adapter);
        initialComponent();

    }
    private void initialComponent(){
        next_step = findViewById(R.id.next_step);

    }
}
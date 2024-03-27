package com.gttcgf.nanoscan.guidingSteps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.gttcgf.nanoscan.DeviceListActivity;
import com.gttcgf.nanoscan.R;

public class IntroGuideActivity extends AppCompatActivity {

    private ImageButton next_step;
    private ViewPager2 viewPager;
    private Button start_binding;
    private ImageView dot_step_1, dot_step_2, dot_step_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_guide);

        viewPager = findViewById(R.id.viewPager);

        GuideStepAdapter adapter = new GuideStepAdapter(this);
        viewPager.setAdapter(adapter);
        initialComponent();

    }
    private void initialComponent(){
        next_step = findViewById(R.id.next_step);
        start_binding = findViewById(R.id.start_binding);
        dot_step_1 = findViewById(R.id.dot_step_1);
        dot_step_2 = findViewById(R.id.dot_step_2);
        dot_step_3 = findViewById(R.id.dot_step_3);

        next_step.setOnClickListener(v -> {
            viewPager = findViewById(R.id.viewPager);
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0){
                    next_step.setVisibility(View.VISIBLE);
                    start_binding.setVisibility(View.GONE);
                    dot_step_1.setImageResource(R.drawable.dot_active);
                    dot_step_2.setImageResource(R.drawable.dot_inactive);
                    dot_step_3.setImageResource(R.drawable.dot_inactive);
                } else if (position == 1) {
                    next_step.setVisibility(View.VISIBLE);
                    start_binding.setVisibility(View.GONE);
                    dot_step_1.setImageResource(R.drawable.dot_inactive);
                    dot_step_2.setImageResource(R.drawable.dot_active);
                    dot_step_3.setImageResource(R.drawable.dot_inactive);
                } else if (position == 2) {
                    next_step.setVisibility(View.GONE);
                    start_binding.setVisibility(View.VISIBLE);
                    dot_step_1.setImageResource(R.drawable.dot_inactive);
                    dot_step_2.setImageResource(R.drawable.dot_inactive);
                    dot_step_3.setImageResource(R.drawable.dot_active);
                }
            }
        });
        start_binding.setOnClickListener(v -> {
            //跳转到设备绑定页面
            Intent intent = new Intent(IntroGuideActivity.this, DeviceListActivity.class);
            startActivity(intent);
            finish();

        });
    }

}
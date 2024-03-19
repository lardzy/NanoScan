package com.gttcgf.nanoscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class GuideStepFragment extends Fragment {
    private static final String ARG_STEP_NUMBER = "step_number";

    public GuideStepFragment() {
        // Required empty public constructor
    }

    public static GuideStepFragment newInstance(int stepNumber) {
        GuideStepFragment fragment = new GuideStepFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STEP_NUMBER, stepNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.guide_step_fragment, container, false);
        ImageView guideImage = view.findViewById(R.id.guideImage);
        TextView guideText = view.findViewById(R.id.guideText);

        // 根据步骤设置图片和文字
        int stepNumber = getArguments().getInt(ARG_STEP_NUMBER, 0);
        // 这里使用switch或if-else根据stepNumber设置图片和文字
        return view;
    }
}

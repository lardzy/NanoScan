package com.gttcgf.nanoscan.guidingSteps;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gttcgf.nanoscan.DeviceList;
import com.gttcgf.nanoscan.R;

public class ThirdStepAragment extends Fragment {
//    private Button start_binding;
    public ThirdStepAragment() {
    }
    public static ThirdStepAragment newInstance() {
        return new ThirdStepAragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        initialComponent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_third_step, container, false);
    }
//    private void initialComponent(){
////        start_binding = getView().findViewById(R.id.start_binding);
//
//    }
}

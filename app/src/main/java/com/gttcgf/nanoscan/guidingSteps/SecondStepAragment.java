package com.gttcgf.nanoscan.guidingSteps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gttcgf.nanoscan.R;

public class SecondStepAragment extends Fragment {
    public SecondStepAragment() {
    }
    public static SecondStepAragment newInstance() {
        return new SecondStepAragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_step, container, false);
    }
}

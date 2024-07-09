package com.gttcgf.nanoscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class ScanResultLineChartFragment extends Fragment {
    public ScanResultLineChartFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = LayoutInflater.from(Objects.requireNonNull(container).getContext()).inflate(R.layout.scan_result_line_chart, container, false);
        View view = inflater.inflate(R.layout.scan_result_line_chart, container, false);
        return view;
    }
}

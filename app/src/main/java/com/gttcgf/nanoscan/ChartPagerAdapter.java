package com.gttcgf.nanoscan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class ChartPagerAdapter extends FragmentStateAdapter {
    // 图表的Fragment
    private final List<ScanResultLineChartFragment> chartFragmentList;

    public ChartPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<ScanResultLineChartFragment> chartFragmentList) {
        super(fragmentActivity);
        this.chartFragmentList = chartFragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return chartFragmentList.get(position);
    }

    public void updateChartData(int position, ArrayList<Entry> newData) {
        ScanResultLineChartFragment chartFragment = chartFragmentList.get(position);
        if (chartFragment != null && newData != null) {
            chartFragment.updateChart(newData);
        }
    }

    @Override
    public int getItemCount() {
        return chartFragmentList.size();
    }

    public List<ScanResultLineChartFragment> getChartFragmentList() {
        return chartFragmentList;
    }
}

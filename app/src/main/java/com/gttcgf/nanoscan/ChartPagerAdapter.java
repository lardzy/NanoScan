package com.gttcgf.nanoscan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ChartPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> chartFragmentList;

    public ChartPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> chartFragmentList) {
        super(fragmentActivity);
        this.chartFragmentList = chartFragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return chartFragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return chartFragmentList.size();
    }
}

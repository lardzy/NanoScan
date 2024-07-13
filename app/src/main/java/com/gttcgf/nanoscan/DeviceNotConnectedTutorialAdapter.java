package com.gttcgf.nanoscan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DeviceNotConnectedTutorialAdapter extends FragmentStateAdapter {

    public DeviceNotConnectedTutorialAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return DeviceNotConnectedDialogTutorialPhotoFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

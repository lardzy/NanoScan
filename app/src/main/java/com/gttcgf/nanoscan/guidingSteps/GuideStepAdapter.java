package com.gttcgf.nanoscan.guidingSteps;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gttcgf.nanoscan.R;

public class GuideStepAdapter extends FragmentStateAdapter {

    public GuideStepAdapter(FragmentActivity fragment) {
        super(fragment);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//         根据位置返回相应的Fragment
        switch (position) {
            case 0:
                return FirstStepAragment.newInstance();
//            case 1:
//                return VideoStepFragment.newInstance(R.raw.guide_open_device, "这是第一个视频下方的文字介绍。");
//            case 2:
//                return VideoStepFragment.newInstance(R.raw.guide_collecting_device, "这是第二个视频下方的文字介绍。");
            default:
                throw new IllegalStateException("Unexpected position: " + position);
//        }
        }
    }
}

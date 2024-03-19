package com.gttcgf.nanoscan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GuideStepAdapter extends FragmentStateAdapter {

    public GuideStepAdapter(FragmentActivity fragment) {
        super(fragment);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public Fragment createFragment(int position) {
        // 根据位置传递不同的视频资源ID和文字
        int videoResId = position == 0 ? R.raw.guide_open_device : R.raw.guide_collecting_device;
        String text = position == 0 ? "这是第一个视频下方的文字介绍。" : "这是第二个视频下方的文字介绍。";
        return VideoStepFragment.newInstance(videoResId, text);
        // 根据位置返回相应的Fragment
//        switch (position) {
//            case 0:
//                return VideoStepFragment.newInstance();
//            case 1:
//                return ImageStepFragment.newInstance();
//            default:
//                throw new IllegalStateException("Unexpected position: " + position);
//        }
    }
}

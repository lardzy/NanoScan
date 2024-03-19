package com.gttcgf.nanoscan;


import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

public class VideoStepFragment extends Fragment {
    private static final String ARG_VIDEO_RES_ID = "video_res_id";
    private static final String ARG_TEXT = "text";
    public VideoStepFragment() {
    }

    public static VideoStepFragment newInstance(int videoResId, String text) {
        VideoStepFragment fragment = new VideoStepFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_VIDEO_RES_ID, videoResId);
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_step, container, false);
        VideoView videoView = view.findViewById(R.id.videoView);
        TextView textView = view.findViewById(R.id.textView);

        if (getArguments() != null) {
            int videoResId = getArguments().getInt(ARG_VIDEO_RES_ID);
            String text = getArguments().getString(ARG_TEXT);

            String path = "android.resource://" + getActivity().getPackageName() + "/" + videoResId;
            videoView.setVideoURI(Uri.parse(path));
            videoView.start();
            textView.setText(text);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        VideoView videoView = getView().findViewById(R.id.videoView);
        if (!videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VideoView videoView = getView().findViewById(R.id.videoView);
        videoView.start();
    }
}

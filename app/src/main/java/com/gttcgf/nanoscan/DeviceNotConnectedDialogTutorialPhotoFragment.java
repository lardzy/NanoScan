package com.gttcgf.nanoscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DeviceNotConnectedDialogTutorialPhotoFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int[] imageResourceID = new int[]
            {R.drawable.check_device_bottom, R.drawable.check_device_front, R.drawable.check_phone_bluetooth};
    private int currentPosition = -1;
    private ImageView iv_prompt_photo;

    public static DeviceNotConnectedDialogTutorialPhotoFragment newInstance(int currentPosition) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, currentPosition);
        DeviceNotConnectedDialogTutorialPhotoFragment fragment = new DeviceNotConnectedDialogTutorialPhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            currentPosition = arguments.getInt(ARG_POSITION, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.troubleshooting_prompt_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_prompt_photo = view.findViewById(R.id.iv_prompt_photo);

        if (currentPosition < imageResourceID.length) {
            iv_prompt_photo.setImageResource(imageResourceID[currentPosition]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

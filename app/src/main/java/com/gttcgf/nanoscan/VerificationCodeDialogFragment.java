package com.gttcgf.nanoscan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class VerificationCodeDialogFragment extends DialogFragment {
    public static VerificationCodeDialogFragment newInstance(String imageUrl) {
        VerificationCodeDialogFragment fragment = new VerificationCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("IMAGE_URL", imageUrl);
        fragment.setArguments(args);
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_verification_code_image, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("验证码")
                .setMessage("这是您的验证码图片")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: Handle the positive button click
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: Handle the negative button click
                    }
                });
        return builder.create();
    }
}

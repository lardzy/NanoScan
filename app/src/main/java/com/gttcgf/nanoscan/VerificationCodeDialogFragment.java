package com.gttcgf.nanoscan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("验证码")
//                .setMessage("这是您的验证码图片")
//                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // TODO: Handle the positive button click
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // TODO: Handle the negative button click
//                        Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        return builder.create();
        return super.onCreateDialog(savedInstanceState);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the buttons in the layout
        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        // Set a click listener for the confirm button
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle the confirm button click
                Toast.makeText(getActivity(), "确认按钮被点击", Toast.LENGTH_SHORT).show();
            }
        });

        // Set a click listener for the cancel button
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle the cancel button click
                Toast.makeText(getActivity(), "取消按钮被点击", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }
}

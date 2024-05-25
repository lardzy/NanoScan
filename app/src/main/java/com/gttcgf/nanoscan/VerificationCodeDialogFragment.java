package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // 在onStart中设置参数，获取到窗口的尺寸
        dialog.setOnShowListener(dialogInterface -> {
            // 获取窗口对象和参数
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();

            // 设置宽度和高度为屏幕的70%
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.3);

            // 将设置好的参数应用到窗口
            window.setAttributes(params);
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "确认按钮被点击", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);

        });

        buttonCancel.setOnClickListener(v -> {
            dismiss();
        });
    }
}

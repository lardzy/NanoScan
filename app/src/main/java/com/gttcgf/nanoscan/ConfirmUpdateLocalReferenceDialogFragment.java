package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmUpdateLocalReferenceDialogFragment extends DialogFragment {
    private View.OnClickListener clickListener;
    private Button button_confirm, button_cancel;

    public static ConfirmUpdateLocalReferenceDialogFragment newInstance() {

        Bundle args = new Bundle();

        ConfirmUpdateLocalReferenceDialogFragment fragment = new ConfirmUpdateLocalReferenceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public ConfirmUpdateLocalReferenceDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // 设置无标题样式
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置不允许点击对话框外部关闭对话框
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public View.OnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_update_local_reference_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button_confirm = view.findViewById(R.id.button_confirm);
        button_cancel = view.findViewById(R.id.button_cancel);
        button_confirm.setOnClickListener(clickListener);
        button_cancel.setOnClickListener(clickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置参数，获取到窗口的尺寸
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 获取窗口对象和参数
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                WindowManager.LayoutParams params = window.getAttributes();
                window.setWindowAnimations(R.style.DialogAnimation);
                // 设置宽度为屏幕的70%
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
//                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
                // 将设置好的参数应用到窗口
                window.setAttributes(params);
            }

        }
    }
}

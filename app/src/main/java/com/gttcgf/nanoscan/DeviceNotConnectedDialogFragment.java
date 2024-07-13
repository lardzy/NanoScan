package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

public class DeviceNotConnectedDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "DeviceNotConnectedDialog";
    private ViewPager2 vp_course_example;
    private Button button_quit;
    private TextView tv_describe;
    private ImageView iv_dot_1, iv_dot_2, iv_dot_3;
    private DeviceNotConnectedTutorialAdapter tutorialAdapter;

    public static DeviceNotConnectedDialogFragment newInstance() {

        Bundle args = new Bundle();

        DeviceNotConnectedDialogFragment fragment = new DeviceNotConnectedDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_not_connected_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vp_course_example = view.findViewById(R.id.vp_course_example);
        button_quit = view.findViewById(R.id.button_quit);
        tv_describe = view.findViewById(R.id.tv_describe);
        iv_dot_1 = view.findViewById(R.id.iv_dot_1);
        iv_dot_2 = view.findViewById(R.id.iv_dot_2);
        iv_dot_3 = view.findViewById(R.id.iv_dot_3);

        button_quit.setOnClickListener(this);

        tutorialAdapter = new DeviceNotConnectedTutorialAdapter(requireActivity());
        vp_course_example.setAdapter(tutorialAdapter);
        vp_course_example.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                upDateDialogDisplay(position);
            }
        });
    }

    private void upDateDialogDisplay(int position) {
        switch (position) {
            case 0:
                iv_dot_1.setImageResource(R.drawable.dot_active);
                iv_dot_2.setImageResource(R.drawable.dot_inactive);
                iv_dot_3.setImageResource(R.drawable.dot_inactive);
                tv_describe.setText(getString(R.string.troubleshooting_prompt_text_step_1));
                break;
            case 1:
                iv_dot_1.setImageResource(R.drawable.dot_inactive);
                iv_dot_2.setImageResource(R.drawable.dot_active);
                iv_dot_3.setImageResource(R.drawable.dot_inactive);
                tv_describe.setText(getString(R.string.troubleshooting_prompt_text_step_2));
                break;
            case 2:
                iv_dot_1.setImageResource(R.drawable.dot_inactive);
                iv_dot_2.setImageResource(R.drawable.dot_inactive);
                iv_dot_3.setImageResource(R.drawable.dot_active);
                tv_describe.setText(getString(R.string.troubleshooting_prompt_text_step_3));
                break;

        }
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
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Dialog dismissed");
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_quit) {
            requireActivity().finish();
        }
    }
}

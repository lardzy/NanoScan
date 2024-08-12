package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceDetailsMenuDialogFragment extends DialogFragment {
    private List<DeviceDetailMenuItems> menuItems;
    private RecyclerView recyclerView;
    private DeviceDetailMenuAdapter deviceDetailMenuAdapter;
    private DeviceItem deviceItem;

    public static DeviceDetailsMenuDialogFragment newInstance(ArrayList<DeviceDetailMenuItems> menuItem, DeviceItem deviceItem) {
        Bundle args = new Bundle();
        DeviceDetailsMenuDialogFragment fragment = new DeviceDetailsMenuDialogFragment(Objects.requireNonNull(menuItem), deviceItem);
        fragment.setArguments(args);
        return fragment;
    }

    public DeviceDetailsMenuDialogFragment(List<DeviceDetailMenuItems> menuItems, DeviceItem deviceItem) {
        this.menuItems = menuItems;
        this.deviceItem = deviceItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceDetailMenuAdapter = new DeviceDetailMenuAdapter(menuItems);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_detail_menu_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(deviceDetailMenuAdapter);

        deviceDetailMenuAdapter.setOnItemClickListener(position -> {
            switch (position) {
                case 0:
                    Toast.makeText(getContext(), "点击了设备详情", Toast.LENGTH_SHORT).show();
                    startDismissAnimation();
                    deviceDetailMenuAdapter.setClickable(false);
                    break;
                case 1:
                    Toast.makeText(getContext(), "点击了删除本地参比", Toast.LENGTH_SHORT).show();
                    startDismissAnimation();
                    deviceDetailMenuAdapter.setClickable(false);
                    break;
                case 2:
                    Toast.makeText(getContext(), "点击了删除当前设备", Toast.LENGTH_SHORT).show();
                    startDismissAnimation();
                    deviceDetailMenuAdapter.setClickable(false);
                    break;
                case 3:
                    Toast.makeText(getContext(), "点击了检索光谱", Toast.LENGTH_SHORT).show();
                    startDismissAnimation();
                    deviceDetailMenuAdapter.setClickable(false);
                    break;
                case 4:
                    Toast.makeText(getContext(), "点击了连接设备", Toast.LENGTH_SHORT).show();
                    startDismissAnimation();
                    deviceDetailMenuAdapter.setClickable(false);
                    break;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置参数，获取窗口尺寸
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 设置窗口对象及参数
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                WindowManager.LayoutParams params = window.getAttributes();
                window.setWindowAnimations(R.style.DialogAnimation);
                params.gravity = Gravity.BOTTOM;
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
                params.width = getResources().getDisplayMetrics().widthPixels;

                window.setAttributes(params);
            }
        }
    }

    private void startDismissAnimation() {
        View view = getView();
        if (view != null) {
            Animation slideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_down);
            slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // 这里在动画结束时调用 dismiss
                    dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.startAnimation(slideOutAnimation);
        } else {
            dismissAllowingStateLoss(); // 如果视图为空，直接关闭对话框
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        startDismissAnimation(); // 当对话框取消时启动动画
    }
}

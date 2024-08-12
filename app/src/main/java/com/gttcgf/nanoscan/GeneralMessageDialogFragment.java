package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class GeneralMessageDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final int MESSAGE_TYPE_ERROR = 0;
    public static final int MESSAGE_TYPE_CHECK = 1;
    private final int MESSAGE_TYPE;
    private String titleContent = "";
    private String informationContent = "";
    private View.OnClickListener clickListener;
    private boolean isDialogFinishActivity = true;
    //region UI组件
    private ImageView iv_icon;
    private TextView tv_title, tv_message_content;
    private Button btn_accept;
    private boolean clickable = true;
    //endregion

    public static GeneralMessageDialogFragment newInstance(int MESSAGE_TYPE, String titleContent, String informationContent) {

        Bundle args = new Bundle();

        GeneralMessageDialogFragment fragment = new GeneralMessageDialogFragment(MESSAGE_TYPE, titleContent, informationContent);
        fragment.setArguments(args);
        return fragment;
    }

    public static GeneralMessageDialogFragment newInstance(int MESSAGE_TYPE, String titleContent, String informationContent, View.OnClickListener clickListener) {

        Bundle args = new Bundle();

        GeneralMessageDialogFragment fragment = new GeneralMessageDialogFragment(MESSAGE_TYPE, titleContent, informationContent, clickListener);
        fragment.setArguments(args);
        return fragment;
    }

    public GeneralMessageDialogFragment(int MESSAGE_TYPE, String titleContent, String informationContent) {
        this.MESSAGE_TYPE = MESSAGE_TYPE;
        this.titleContent = titleContent;
        this.informationContent = informationContent;
    }

    public GeneralMessageDialogFragment(int MESSAGE_TYPE, String titleContent, String informationContent, View.OnClickListener clickListener) {
        this.MESSAGE_TYPE = MESSAGE_TYPE;
        this.titleContent = titleContent;
        this.informationContent = informationContent;
        this.clickListener = clickListener;
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
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.general_message_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_icon = view.findViewById(R.id.iv_icon);
        tv_title = view.findViewById(R.id.tv_title);
        tv_message_content = view.findViewById(R.id.tv_message_content);
        btn_accept = view.findViewById(R.id.btn_accept);

        if (MESSAGE_TYPE == MESSAGE_TYPE_ERROR) {
            iv_icon.setImageResource(R.drawable.baseline_error_24);
        } else if (MESSAGE_TYPE == MESSAGE_TYPE_CHECK) {
            iv_icon.setImageResource(R.drawable.baseline_check_circle_24);
        }
        tv_title.setText(titleContent);
        tv_message_content.setText(informationContent);
        if (clickListener != null) {
            btn_accept.setOnClickListener(clickListener);
        } else {
            btn_accept.setOnClickListener(this);
        }
        clickable = true;
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
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
                params.width = getResources().getDisplayMetrics().widthPixels;

                window.setAttributes(params);
            }
        }
    }

    public boolean isDialogFinishActivity() {
        return isDialogFinishActivity;
    }

    public void setDialogFinishActivity(boolean dialogFinishActivity) {
        isDialogFinishActivity = dialogFinishActivity;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        View view = getView();
        if (view != null) {
            // 这个动画写得一坨...背景似乎不会跟着下降
            Animation slideOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_down);
            slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    GeneralMessageDialogFragment.super.onDismiss(dialog);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.startAnimation(slideOutAnimation);
        } else {
            super.onDismiss(dialog);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_accept && clickable) {
            clickable = false;
            if (isDialogFinishActivity) {
                requireActivity().finish();
            } else {
                onDismiss(Objects.requireNonNull(getDialog()));
            }
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}

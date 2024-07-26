package com.gttcgf.nanoscan;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileFunctionAdapter extends RecyclerView.Adapter<UserProfileFunctionAdapter.UserProfileFunctionViewHolder> {
    private static final String TAG = "UserProfileFunctionAdapter";
    private List<UserProfileFunctionItem> functionItems;
    private boolean isClickable = true;
    private OnItemClickListener onItemClickListener;

    public UserProfileFunctionAdapter(List<UserProfileFunctionItem> functionItems, OnItemClickListener onItemClickListener) {
        this.functionItems = Objects.requireNonNull(functionItems);
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public UserProfileFunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile_function_item, parent, false);
        return new UserProfileFunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProfileFunctionViewHolder holder, int position) {
        holder.tv_function_name.setText(functionItems.get(position).getFunctionDescription());
        holder.iv_function_icon.setImageResource(functionItems.get(position).getResourceID());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && isClickable) {
                    Log.d(TAG, "用户信息功能列表适配器-项目条被点击：" + currentPosition);
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(currentPosition);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return functionItems.size();
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public static class UserProfileFunctionViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_function_icon;
        private TextView tv_function_name;

        public UserProfileFunctionViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_function_name = itemView.findViewById(R.id.tv_function_name);
            iv_function_icon = itemView.findViewById(R.id.iv_function_icon);
        }
    }
}

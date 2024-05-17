package com.gttcgf.nanoscan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {
    private List<DeviceItem> deviceItems;
    private OnItemClickListener myItemClickListener;

    public DeviceListAdapter(List<DeviceItem> deviceItems) {
        this.deviceItems = deviceItems;
    }

    // 创建ViewHolder的实例，并把加载的布局传入ViewHolder的构造函数中。
    @NonNull
    @Override
    public DeviceListAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new DeviceViewHolder(v);
    }

    // 用于对子项数据进行赋值，当子项进入屏幕内时执行。
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceItem currentItem = deviceItems.get(position);
        holder.card_image.setImageResource(currentItem.getImageResource());
        holder.card_title.setText(currentItem.getDeviceName());
        holder.card_title_1.setText(currentItem.getDeviceType());
        holder.card_id.setText(String.valueOf(position + 1));

        holder.itemView.setOnClickListener(view -> {
            myItemClickListener.OnItemClick(position);
        });
        holder.itemView.setOnLongClickListener(view -> {
            myItemClickListener.OnItemLongClick(position);
            return true; // 返回true表示这个事件已经被处理，不会再传递给其他的监听器
        });
    }

    // 返回RecyclerView子项的数目
    @Override
    public int getItemCount() {
        return deviceItems.size();
    }

    // 自定义的ViewHolder类，继承自RecyclerView.ViewHolder
    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        public ImageView card_image;
        public TextView card_title, card_title_1, card_id;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            card_image = itemView.findViewById(R.id.card_image);
            card_title = itemView.findViewById(R.id.card_title);
            card_title_1 = itemView.findViewById(R.id.card_title_1);
            card_id = itemView.findViewById(R.id.card_id);
        }
    }

    public void setOnItemClickListener(OnItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

    interface OnItemClickListener {
        void OnItemClick(int position);

        void OnItemLongClick(int position);
    }
}
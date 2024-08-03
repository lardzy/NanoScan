package com.gttcgf.nanoscan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class DeviceDetailMenuAdapter extends RecyclerView.Adapter<DeviceDetailMenuAdapter.DeviceDetailMenuAdapterViewHolder> {
    private List<DeviceDetailMenuItems> menuItems;
    private MainActivityDeviceListAdapter.OnItemClickListener onItemClickListener;
    private boolean isClickable = true;

    public DeviceDetailMenuAdapter(List<DeviceDetailMenuItems> menuItems) {
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public DeviceDetailMenuAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_detail_menu_dialog_items, parent, false);
        return new DeviceDetailMenuAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceDetailMenuAdapterViewHolder holder, int position) {
        holder.tv_menu_item.setText(menuItems.get(position).getFunctionName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                // 当项目条在视野内，并且isClickable为true时，绑定点击事件
                if (currentPosition != RecyclerView.NO_POSITION && isClickable) {
                    onItemClickListener.OnItemClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class DeviceDetailMenuAdapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_menu_item;

        public DeviceDetailMenuAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_menu_item = itemView.findViewById(R.id.tv_menu_item);
        }
    }

    public List<DeviceDetailMenuItems> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<DeviceDetailMenuItems> menuItems) {
        this.menuItems = menuItems;
    }

    public void setOnItemClickListener(MainActivityDeviceListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = Objects.requireNonNull(onItemClickListener);
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
}

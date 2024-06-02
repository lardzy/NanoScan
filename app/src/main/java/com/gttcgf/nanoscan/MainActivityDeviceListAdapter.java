package com.gttcgf.nanoscan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivityDeviceListAdapter extends RecyclerView.Adapter<MainActivityDeviceListAdapter.MainActivityDeviceViewHolder> {
    private List<DeviceItem> deviceItemList;
    private OnItemClickListener onItemClickListener;

    public MainActivityDeviceListAdapter(List<DeviceItem> deviceItemList) {
        this.deviceItemList = deviceItemList;
    }

    @NonNull
    @Override
    public MainActivityDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_for_main, parent, false);

        return new MainActivityDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityDeviceViewHolder holder, int position) {
        DeviceItem currentItem = deviceItemList.get(position);
        holder.card_id.setText(String.valueOf(position + 1));
        holder.tv_main_nano_name.setText(currentItem.getDeviceName());
        holder.tv_main_nano_mac.setText(currentItem.getDeviceMac());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener.OnItemClick(holder.getAdapterPosition());
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return deviceItemList.size();
    }

    public static class MainActivityDeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_main_nano_name, tv_main_nano_mac, card_main_device_type, card_id;

        public MainActivityDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_main_nano_name = itemView.findViewById(R.id.tv_main_nano_name);
            tv_main_nano_mac = itemView.findViewById(R.id.tv_main_nano_mac);
            card_main_device_type = itemView.findViewById(R.id.card_main_device_type);
            card_id = itemView.findViewById(R.id.card_id);

        }
    }

    interface OnItemClickListener {
        void OnItemClick(int position);
    }
}

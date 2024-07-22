package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class MainActivityDeviceListAdapter extends RecyclerView.Adapter<MainActivityDeviceListAdapter.MainActivityDeviceViewHolder> {
    private static final String TAG = "MainActivityDeviceListA";
    private List<DeviceItem> deviceItemList;
    private OnItemClickListener onItemClickListener;
    private boolean isClickable = true;

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
                // 当项目条在视野内，并且isClickable为true时，绑定点击事件
                if (currentPosition != RecyclerView.NO_POSITION && isClickable) {
                    onItemClickListener.OnItemClick(holder.getAdapterPosition());
                }
            }
        });
    }

    // 对比列表对象与传入列表对象之间是否有差异，有差异则更新列表。
    public void updateDeviceList(List<DeviceItem> newDeviceList) {
        Log.d(TAG, "主界面设备列表adapter-updateDeviceList被调用，正在尝试更新列表！");

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return deviceItemList.size();
            }

            @Override
            public int getNewListSize() {
                return newDeviceList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(deviceItemList.get(oldItemPosition).getDeviceMac(), newDeviceList.get(newItemPosition).getDeviceMac());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return deviceItemList.get(oldItemPosition).equals(newDeviceList.get(newItemPosition));
            }
        });

        Log.d(TAG, "主界面设备列表adapter-updateDeviceList更新deviceItemList！");
        // 更新数据
        deviceItemList.clear();
        deviceItemList.addAll(newDeviceList);
        Log.d(TAG, "主界面设备列表adapter-updateDeviceList分发事件更新！");
        // 分发更新事件
        diffResult.dispatchUpdatesTo(this);
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
    // 更新适配器数据并通知数据集变化
    @SuppressLint("NotifyDataSetChanged")
    public void filterList(List<DeviceItem> filteredList) {
        deviceItemList = filteredList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
}

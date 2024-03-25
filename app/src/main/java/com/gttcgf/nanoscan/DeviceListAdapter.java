package com.gttcgf.nanoscan;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>{
    private List<DeviceItem> deviceItems;

    public DeviceListAdapter(List<DeviceItem> deviceItems) {
        this.deviceItems = deviceItems;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceItem currentItem = deviceItems.get(position);
        holder.card_image.setImageResource(currentItem.getImageResource());
        holder.card_title.setText(currentItem.getTitle());
        holder.card_title_1.setText(currentItem.getTitle1());
    }

    @Override
    public int getItemCount() {
        return deviceItems.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        public ImageView card_image;
        public TextView card_title, card_title_1;
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            card_image = itemView.findViewById(R.id.card_image);
            card_title = itemView.findViewById(R.id.card_title);
            card_title_1 = itemView.findViewById(R.id.card_title_1);

        }
    }
}

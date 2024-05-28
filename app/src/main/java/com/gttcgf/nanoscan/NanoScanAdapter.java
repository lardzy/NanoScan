package com.gttcgf.nanoscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ISCSDK.ISCNIRScanSDK;

import java.util.ArrayList;

public class NanoScanAdapter extends ArrayAdapter<ISCNIRScanSDK.NanoDevice> {
    private final ArrayList<ISCNIRScanSDK.NanoDevice> nanoDevices;
    private Context context;

    public NanoScanAdapter(@NonNull Context context, ArrayList<ISCNIRScanSDK.NanoDevice> values) {
        super(context, -1, values);
        this.nanoDevices = values;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_nano_scan_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tv_nano_name = convertView.findViewById(R.id.tv_nano_name);
            viewHolder.card_mac_address = convertView.findViewById(R.id.card_mac_address);
            viewHolder.tv_rssi = convertView.findViewById(R.id.tv_rssi);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ISCNIRScanSDK.NanoDevice device = getItem(position);
        if (device != null) {
            viewHolder.tv_nano_name.setText(device.getNanoName());
            viewHolder.card_mac_address.setText(device.getNanoMac());
            viewHolder.tv_rssi.setText(device.getRssiString());
        }
        return convertView;
    }

    private static class ViewHolder {
        private TextView tv_nano_name, card_mac_address, tv_rssi;
    }
}

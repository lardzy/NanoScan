package com.gttcgf.nanoscan;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gttcgf.nanoscan.tools.SpectralDataUtils;

public class RecentSpectralDataListAdapter extends RecyclerView.Adapter<RecentSpectralDataListAdapter.DataListViewHolder> {


    @NonNull
    @Override
    public DataListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DataListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return SpectralDataUtils.userSpectralFileSet.size();
    }

    public static class DataListViewHolder extends RecyclerView.ViewHolder {

        public DataListViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

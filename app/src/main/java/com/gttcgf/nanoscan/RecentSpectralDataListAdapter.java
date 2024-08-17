package com.gttcgf.nanoscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RecentSpectralDataListAdapter extends RecyclerView.Adapter<RecentSpectralDataListAdapter.DataListViewHolder> {

    private List<String> dataList;
    private Context mContext;

    public RecentSpectralDataListAdapter(Context mContext) {
        this.mContext = mContext;
        updateDataList();
    }

    // 获取最新的保存预测结果索引Map集合
    public void updateDataList() {
        if (SpectralDataUtils.userSpectralFileMap != null) {
            this.dataList = new ArrayList<>(SpectralDataUtils.userSpectralFileMap.keySet());
            Collections.reverse(dataList);
        }
    }

    @NonNull
    @Override
    public DataListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_for_spectral, parent, false);
        return new DataListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataListViewHolder holder, int position) {
        holder.spectrum_id.setText(String.valueOf((position + 1)));
        holder.tv_date.setText(mContext.getString(R.string.spectrum_date, Objects.requireNonNull(SpectralDataUtils.userSpectralFileMap.get(dataList.get(position))).getDateTime()));
        holder.tv_predict_result.setText(mContext.getString(R.string.spectrum_predict_result, Objects.requireNonNull(SpectralDataUtils.userSpectralFileMap.get(dataList.get(position))).getPredictResult()));
    }

    @Override
    public int getItemCount() {
        return SpectralDataUtils.userSpectralFileMap.size();
    }

    public static class DataListViewHolder extends RecyclerView.ViewHolder {
        private TextView spectrum_id, tv_date, tv_predict_result;

        public DataListViewHolder(@NonNull View itemView) {
            super(itemView);
            spectrum_id = itemView.findViewById(R.id.scan_result_card_id);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_predict_result = itemView.findViewById(R.id.tv_predict_result);
        }
    }
}

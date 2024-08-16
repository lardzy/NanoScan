package com.gttcgf.nanoscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class PredictResultListAdapter extends RecyclerView.Adapter<PredictResultListAdapter.PredictResultViewHolder> {
    private List<PredictResult> predictResults;
    private Context mContext;

    public PredictResultListAdapter(List<PredictResult> predictResults, Context mContext) {
        this.predictResults = Objects.requireNonNull(predictResults);
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PredictResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_for_predict_result, parent, false);
        return new PredictResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictResultViewHolder holder, int position) {
        holder.tv_result.setText(mContext.getString(R.string.scan_result_list, String.valueOf(position + 1),
                predictResults.get(position).getMaterial(),
                String.valueOf(predictResults.get(position).getPercentage())));
    }

    @Override
    public int getItemCount() {
        return predictResults.size();
    }

    public static class PredictResultViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_result;

        public PredictResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_result = itemView.findViewById(R.id.tv_result);
        }
    }
}

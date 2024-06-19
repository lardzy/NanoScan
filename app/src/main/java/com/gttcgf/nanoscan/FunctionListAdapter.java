package com.gttcgf.nanoscan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FunctionListAdapter extends RecyclerView.Adapter<FunctionListAdapter.FunctionListViewHolder> {
    private List<FunctionItem> functionList = new ArrayList<>();
    private Context context;
    private int selectedItemPosition = -1;
    private boolean isClickable = true;

    public FunctionListAdapter(Context context, List<FunctionItem> functionList) {
        this.functionList = functionList;
        this.context = context;
    }

    @NonNull
    @Override
    public FunctionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_for_scan_function, parent, false);
        return new FunctionListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionListViewHolder holder, int position) {
        FunctionItem functionItem = functionList.get(position);
//        holder.iv_scan_function.setImageResource(functionItem.getImageResId());
        holder.iv_scan_function.setBackground(ContextCompat.getDrawable(context, functionItem.getImageResId()));
        holder.tv_scan_function_mode.setText(functionItem.getFunctionName());
        holder.tv_scan_function_content.setText(functionItem.getFunctionDescription());

        // 更新选中或未选中的列表UI
        if (functionItem.isSelected()) {  // 当选中
            holder.cl_scan_function.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_blue));
            holder.iv_scan_function.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
            holder.tv_scan_function_mode.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tv_scan_function_content.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else { // 当未选中
//            holder.cl_scan_function.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.cl_scan_function.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rectangle));
            holder.iv_scan_function.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.black));
            holder.tv_scan_function_mode.setTextColor(ContextCompat.getColor(context, R.color.gray));
            holder.tv_scan_function_content.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                if (isClickable) {
                    selectedItemPosition = holder.getAdapterPosition();
                    updateClickStatus();
                    // 更新UI
                    notifyDataSetChanged();
                }
            }
        });

    }

    // 更新列表的选中状态
    private void updateClickStatus() {
        FunctionItem functionItem = functionList.get(selectedItemPosition);
        boolean singleChoice = functionItem.isSingleChoice();

        if (!singleChoice) {  // 当选中的为可以多选的选项，则只翻转选中状态，不更新列表其他项目。
            functionItem.setSelected(!functionItem.isSelected());
        } else {  // 当选中的为单选的选项，遍历列表，将选中位置的选项更新为选中，未选中位置更新为未选中，多选的选项不进行操作。
            for (int i = 0; i < functionList.size(); i++) {
                FunctionItem item = functionList.get(i);
                if (i == selectedItemPosition) {
                    item.setSelected(true);
                    continue;
                }
                if (item.isSingleChoice()) {
                    item.setSelected(false);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return functionList.size();
    }

    public static class FunctionListViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_scan_function;
        public TextView tv_scan_function_mode, tv_scan_function_content;
        public ConstraintLayout cl_scan_function;

        public FunctionListViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_scan_function = itemView.findViewById(R.id.iv_scan_function);
            tv_scan_function_mode = itemView.findViewById(R.id.tv_scan_function_mode);
            tv_scan_function_content = itemView.findViewById(R.id.tv_scan_function_content);
            cl_scan_function = itemView.findViewById(R.id.cl_scan_function);
        }
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
}

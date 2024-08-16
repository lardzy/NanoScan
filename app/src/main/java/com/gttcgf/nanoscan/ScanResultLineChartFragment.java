package com.gttcgf.nanoscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ScanResultLineChartFragment extends Fragment {
    private static final String TAG = "ScanResultLineChartFrag";
    private static final String ARG_CHART_TYPE = "chart_type";
    private static final String ARG_DATA = "data";
    private int CHART_TYPE;
    public static final int CHART_ABSORBANCE = 0;  // 吸光度
    public static final int CHART_REFLECTANCE = 1;  // 反射率
    public static final int CHART_INTENSITY = 2;  // 光强度
    public static final int CHART_REFERENCE = 3;  // 参比
    private String tabText = "";


    private final List<Entry> chartData = new ArrayList<>();
    private LineChart lineChart;
    private LineData lineData;
    private LineDataSet lineDataSet;

    public static ScanResultLineChartFragment newInstance(int CHART_TYPE, ArrayList<Entry> data) {
        ScanResultLineChartFragment scanResultLineChartFragment = new ScanResultLineChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CHART_TYPE, CHART_TYPE);
        args.putSerializable(ARG_DATA, data);
        scanResultLineChartFragment.setArguments(args);

        return scanResultLineChartFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d(TAG, "ScanResultLineChartFragment onCreate called.");
            this.CHART_TYPE = getArguments().getInt(ARG_CHART_TYPE);
            this.chartData.clear();
            this.chartData.addAll((List<Entry>) getArguments().getSerializable(ARG_DATA));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scan_result_line_chart, container, false);
        lineChart = view.findViewById(R.id.lc_chart);
        // 初始化图表
        setupChart();
        return view;
    }

    // 初始化图表
    private void setupChart() {
        lineDataSet = new LineDataSet(chartData, getTabText());
        lineData = new LineData(lineDataSet);
        lineChart.setNoDataText("当前无数据");
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);  // 背景网格不显示
        lineChart.setPinchZoom(true);  // 开启双指放大（按比例放大）
        lineChart.getDescription().setText("");  // 不显示右下角描述
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);
        Context context = getContext();
        if (context != null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.fade_blue);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
        }
        // 不显示数据点顶端的小圆点
        lineDataSet.setDrawIcons(false); // 不显示端点的icon
        lineDataSet.setDrawCircles(false); // 显示顶点圆圈
        lineDataSet.setDrawCircleHole(true); // 空心还是实心
        lineDataSet.setDrawValues(false);  // 不显示具体数值

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);

        lineChart.animateXY(1000, 1000, Easing.EaseInOutCubic);
        // 当数据表为空时，显示提示文字
        if (chartData.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate(); // 刷新图表以显示提示文本
        } else {
            lineChart.setData(lineData);
            lineChart.invalidate();
        }
    }
    // 更新光谱数据
    public void updateChart(ArrayList<Entry> newData) {
        chartData.clear();
        chartData.addAll(newData);
        // 如果当前Fragment没有被isAdded()到activity中，可能会空指针异常。
        if (lineDataSet != null && lineData != null) {
            lineChart.setData(lineData);  // 当表格初始化时由于数据为空，未设置lineData，于此处更新时设置
            lineDataSet.notifyDataSetChanged();
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();

            lineChart.animateXY(1000, 1000, Easing.EaseInOutCubic);
            lineChart.invalidate(); // 刷新图表数据
        }
    }

    public String getTabText() {
        switch (CHART_TYPE) {
            case CHART_ABSORBANCE:
                tabText = "吸光度";
                break;
            case CHART_REFLECTANCE:
                tabText = "反射率";
                break;
            case CHART_INTENSITY:
                tabText = "光强度";
                break;
            case CHART_REFERENCE:
                tabText = "参比";
                break;
        }
        return tabText;
    }

    public int getCHART_TYPE() {
        return CHART_TYPE;
    }
}

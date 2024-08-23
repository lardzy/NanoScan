package com.gttcgf.nanoscan;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpectralPreviewDialogFragment extends DialogFragment implements View.OnClickListener {
    private Context mContext;
    private Handler mHandler;
    private List<ScanResultLineChartFragment> charts = new ArrayList<>();
    private TabLayout spectral_tabLayout;
    private ViewPager2 vp_spectral_chart_pages;
    private TextView tv_preview_result;
    private Button btn_button_cancel;
    private NirSpectralData nirSpectralData;
    private String userPhoneNumber, fileName;
    private ChartPagerAdapter chartPagerAdapter;

    public static SpectralPreviewDialogFragment newInstance(Bundle bundle) {
        SpectralPreviewDialogFragment fragment = new SpectralPreviewDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        mHandler = new Handler();
        initialData();
    }

    private void initialData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            userPhoneNumber = bundle.getString("userPhoneNumber");
            fileName = bundle.getString("fileName");
        }
        nirSpectralData = SpectralDataUtils.readNirSpectralDataFromFile(mContext, userPhoneNumber, fileName);
        if (nirSpectralData != null) {
            // 添加4种图表
            charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_ABSORBANCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmAbsorbanceFloat())));
            charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_REFLECTANCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmReflectanceFloat())));
            charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_INTENSITY, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmIntensityFloat())));
            charts.add(ScanResultLineChartFragment.newInstance(ScanResultLineChartFragment.CHART_REFERENCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmReferenceFloat())));

        }
    }

    public void updateChartData() {
        // 更新4张图表
        chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_ABSORBANCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmAbsorbanceFloat()));
        chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_REFLECTANCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmReflectanceFloat()));
        chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_INTENSITY, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmIntensityFloat()));
        chartPagerAdapter.updateChartData(ScanResultLineChartFragment.CHART_REFERENCE, SpectralDataUtils.nirSpectralDataProcessor(Objects.requireNonNull(nirSpectralData).getmReferenceFloat()));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // 设置无标题样式
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return getLayoutInflater().inflate(R.layout.fragment_spectral_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spectral_tabLayout = view.findViewById(R.id.spectral_tabLayout);
        vp_spectral_chart_pages = view.findViewById(R.id.vp_spectral_chart_pages);
        tv_preview_result = view.findViewById(R.id.tv_preview_result);
        btn_button_cancel = view.findViewById(R.id.btn_button_cancel);
        chartPagerAdapter = new ChartPagerAdapter(requireActivity(), charts);
        vp_spectral_chart_pages.setAdapter(chartPagerAdapter);
        btn_button_cancel.setOnClickListener(this);
        new TabLayoutMediator(spectral_tabLayout, vp_spectral_chart_pages, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.tab_absorbance);
                        break;
                    case 1:
                        tab.setText(R.string.tab_reflectance);
                        break;
                    case 2:
                        tab.setText(R.string.tab_intensity);
                        break;
                    case 3:
                        tab.setText(R.string.tab_Reference);
                        break;
                }
            }
        }).attach();
        if (nirSpectralData != null) {
            tv_preview_result.setText(getString(R.string.spectrum_predict_result_preview,
                    nirSpectralData.getDateTime(), nirSpectralData.getPredictResultsDescription()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置参数，获取到窗口的尺寸
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.rounded_rectangle);
                // 获取窗口对象和参数
                WindowManager.LayoutParams params = window.getAttributes();
                // 设置宽度和高度为屏幕的70%
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);
                // 将设置好的参数应用到窗口
                window.setAttributes(params);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_button_cancel) {
            dismiss();
        }
    }
}

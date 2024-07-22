package com.gttcgf.nanoscan;

import com.github.mikephil.charting.data.Entry;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NirSpectralData implements Serializable {
    // 版本
    private static final long serialVersionUID = 1L;
    // todo:以设备MAC为区分，需要可以导出图表数据点、导出csv数据、记录采集参数（设备参数、用户参数）、存储的光谱数据的文件名。
    private final String deviceMAC;
    //    private final String fileName;
    // 光谱强度数据（即为原始数据）
    private final ArrayList<Entry> mIntensityFloat;
    // 吸光度
    private final ArrayList<Entry> mAbsorbanceFloat;
    // 参比
    private final ArrayList<Entry> mReferenceFloat;
    // 波长范围
    private final ArrayList<Float> mWavelengthFloat;
    private ArrayList<PredictResult> predictResults = new ArrayList<>();
    private PredictResult predictResult;
    private final String predictSessionUUID;

    public NirSpectralData(String deviceMAC, ArrayList<Entry> mIntensityFloat, ArrayList<Entry> mAbsorbanceFloat, ArrayList<Entry> mReferenceFloat, ArrayList<Float> mWavelengthFloat, String predictSessionUUID) {
        this.deviceMAC = deviceMAC;
        this.mIntensityFloat = mIntensityFloat;
        this.mAbsorbanceFloat = mAbsorbanceFloat;
        this.mReferenceFloat = mReferenceFloat;
        this.mWavelengthFloat = mWavelengthFloat;
        this.predictSessionUUID = predictSessionUUID;
        createFileName();
    }

    private void createFileName() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String formatDate = simpleDateFormat.format(date);
    }

    public PredictResult getPredictResult() {
        return predictResult;
    }

    public void setPredictResult(PredictResult predictResult) {
        this.predictResult = predictResult;
    }
}

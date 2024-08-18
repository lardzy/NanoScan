package com.gttcgf.nanoscan;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NirSpectralData implements Serializable {
    // 版本
    private static final long serialVersionUID = 1L;
    // todo:以设备MAC为区分，需要可以导出图表数据点、导出csv数据、记录采集参数（设备参数、用户参数）、存储的光谱数据的文件名。
    private final String deviceMAC;
    // 光谱横坐标
    private final ArrayList<String> mXValues;
    // 吸光度
    private final ArrayList<mEntry> mAbsorbanceFloat;
    // 反射率
    private final ArrayList<mEntry> mReflectanceFloat;
    // 光谱强度数据（即为原始数据）
    private final ArrayList<mEntry> mIntensityFloat;
    // 参比
    private final ArrayList<mEntry> mReferenceFloat;
    private List<PredictResult> predictResults = new ArrayList<>();
    private final String predictSessionUUID;
    private String fileNamePrefix, dateTime;


    public NirSpectralData(String deviceMAC, ArrayList<String> mXValues, ArrayList<mEntry> mAbsorbanceFloat, ArrayList<mEntry> mReflectanceFloat, ArrayList<mEntry> mIntensityFloat, ArrayList<mEntry> mReferenceFloat, String predictSessionUUID) {
        this.deviceMAC = deviceMAC;
        this.mXValues = mXValues;
        this.mAbsorbanceFloat = mAbsorbanceFloat;
        this.mReflectanceFloat = mReflectanceFloat;
        this.mIntensityFloat = mIntensityFloat;
        this.mReferenceFloat = mReferenceFloat;
        this.predictSessionUUID = predictSessionUUID;
        createFileName();
    }

    private void createFileName() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String formatDate = simpleDateFormat.format(date);
        dateTime = formatDate;
        fileNamePrefix = deviceMAC + "_" + formatDate;
    }

    public ArrayList<String> getmXValues() {
        return mXValues;
    }

    public ArrayList<mEntry> getmAbsorbanceFloat() {
        return mAbsorbanceFloat;
    }

    public ArrayList<mEntry> getmReflectanceFloat() {
        return mReflectanceFloat;
    }

    public ArrayList<mEntry> getmIntensityFloat() {
        return mIntensityFloat;
    }

    public ArrayList<mEntry> getmReferenceFloat() {
        return mReferenceFloat;
    }

    public List<PredictResult> getPredictResults() {
        return predictResults;
    }

    public String getPredictSessionUUID() {
        return predictSessionUUID;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public String getDeviceMAC() {
        return deviceMAC;
    }

    public void setPredictResults(List<PredictResult> predictResults) {
        this.predictResults = predictResults;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getPredictResultsDescription() {
        if (predictResults != null) {
            StringBuilder sb = new StringBuilder();
            for (PredictResult predictResult : predictResults) {
                sb.append(predictResult.getMaterial());
                sb.append(":");
                sb.append(predictResult.getPercentage());
                sb.append(";");
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}

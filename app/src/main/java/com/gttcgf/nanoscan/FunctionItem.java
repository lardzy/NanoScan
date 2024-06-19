package com.gttcgf.nanoscan;

import java.util.Objects;

public class FunctionItem {
    private String functionName, functionDescription;
    // 存放图片资源
    private int imageResId;
    // 是否是单选
    private boolean isSingleChoice = true;
    private boolean isSelected = false;

    public FunctionItem(String functionName, String functionDescription, int imageResId, boolean isSingleChoice) {
        this.functionName = functionName;
        this.functionDescription = functionDescription;
        this.imageResId = imageResId;
        this.isSingleChoice = isSingleChoice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionItem that = (FunctionItem) o;
        return Objects.equals(functionName, that.functionName) && Objects.equals(functionDescription, that.functionDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, functionDescription);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSingleChoice() {
        return isSingleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        isSingleChoice = singleChoice;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}

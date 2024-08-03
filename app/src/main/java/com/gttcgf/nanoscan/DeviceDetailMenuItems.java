package com.gttcgf.nanoscan;

import java.util.Objects;

public class DeviceDetailMenuItems {
    private String functionName;

    public DeviceDetailMenuItems(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailMenuItems that = (DeviceDetailMenuItems) o;
        return Objects.equals(functionName, that.functionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(functionName);
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}

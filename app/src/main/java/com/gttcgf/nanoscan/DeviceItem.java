package com.gttcgf.nanoscan;

import java.io.Serializable;
import java.util.Objects;

public class DeviceItem implements Serializable {
    private int ImageResource;
    private static final long serialVersionUID = 1L;
    private String deviceName, deviceType, deviceMac, deviceToken;

    public DeviceItem() {
    }

    public DeviceItem(int imageResource, String deviceName, String deviceType, String deviceMac, String deviceToken) {
        ImageResource = imageResource;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceMac = deviceMac;
        this.deviceToken = deviceToken;
    }

    public int getImageResource() {
        return ImageResource;
    }

    public void setImageResource(int imageResource) {
        ImageResource = imageResource;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        return "DeviceItem{" +
                "ImageResource=" + ImageResource +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                ", deviceToken='" + deviceToken + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceItem that = (DeviceItem) o;
        return Objects.equals(deviceName, that.deviceName) && Objects.equals(deviceMac, that.deviceMac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName, deviceMac);
    }
}

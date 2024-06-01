package com.gttcgf.nanoscan;

import java.io.Serializable;

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
}

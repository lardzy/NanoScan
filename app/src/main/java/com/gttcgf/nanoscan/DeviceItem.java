package com.gttcgf.nanoscan;

public class DeviceItem {
    private int ImageResource;
    private String deviceName, deviceType;

    public DeviceItem(int imageResource, String deviceName, String deviceType) {
        ImageResource = imageResource;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
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
}

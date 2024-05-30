package com.gttcgf.nanoscan;

public class DeviceItem {
    private int ImageResource;
    private String deviceName, deviceType, deviceMac;

    public DeviceItem(int imageResource, String deviceName, String deviceType, String deviceMac) {
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

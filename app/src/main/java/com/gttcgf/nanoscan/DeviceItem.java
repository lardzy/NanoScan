package com.gttcgf.nanoscan;

import java.io.Serializable;
import java.util.Objects;

public class DeviceItem implements Serializable {
    private int ImageResource;
    private static final long serialVersionUID = 1L;
    // deviceName默认是设备蓝牙的名称，deviceType是设备类型，deviceMac是设备MAC地址，deviceToken是设备验证token。
    private String user, deviceName, deviceType, deviceMac, deviceToken;
    private final String deviceBluetoothName;

    public DeviceItem(String userPhoneNumber, int imageResource, String deviceName, String deviceType, String deviceMac, String deviceToken) {
        this.user = userPhoneNumber;
        this.ImageResource = imageResource;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceMac = deviceMac;
        this.deviceToken = deviceToken;
        this.deviceBluetoothName = deviceName;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceBluetoothName() {
        return deviceBluetoothName;
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

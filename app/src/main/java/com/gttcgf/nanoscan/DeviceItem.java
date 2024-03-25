package com.gttcgf.nanoscan;

public class DeviceItem {
    private int ImageResource;
    private String Title, Title1;

    public DeviceItem(int imageResource, String title, String title1) {
        ImageResource = imageResource;
        Title = title;
        Title1 = title1;
    }

    public int getImageResource() {
        return ImageResource;
    }

    public void setImageResource(int imageResource) {
        ImageResource = imageResource;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle1() {
        return Title1;
    }

    public void setTitle1(String title1) {
        Title1 = title1;
    }
}

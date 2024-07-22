package com.gttcgf.nanoscan;

public class UserProfileFunctionItem {
    private int resourceID;
    private String functionDescription;

    public UserProfileFunctionItem(int resourceID, String functionDescription) {
        this.resourceID = resourceID;
        this.functionDescription = functionDescription;
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }
}

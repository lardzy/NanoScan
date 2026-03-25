package com.gttcgf.nanoscan.data.model;

public class UserSession {
    private final String phoneNumber;
    private final String password;
    private final String token;
    private final String ipAddress;
    private final boolean firstRun;
    private final boolean userAgreed;

    public UserSession(String phoneNumber, String password, String token, String ipAddress, boolean firstRun, boolean userAgreed) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.token = token;
        this.ipAddress = ipAddress;
        this.firstRun = firstRun;
        this.userAgreed = userAgreed;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public boolean isUserAgreed() {
        return userAgreed;
    }
}

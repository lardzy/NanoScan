package com.gttcgf.nanoscan.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.ISCSDK.ISCNIRScanSDK;

public class ScanSdkRepository {
    private final Context appContext;
    private final SdkBroadcastRegistry broadcastRegistry;

    public ScanSdkRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.broadcastRegistry = new SdkBroadcastRegistry(appContext);
    }

    public SdkBroadcastRegistry getBroadcastRegistry() {
        return broadcastRegistry;
    }

    public void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, ISCNIRScanSDK.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context, ServiceConnection serviceConnection) {
        try {
            context.unbindService(serviceConnection);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void startScan() {
        ISCNIRScanSDK.StartScan();
    }

    public void getDeviceStatus() {
        ISCNIRScanSDK.GetDeviceStatus();
    }

    public void requestSpectrumCoefficients() {
        ISCNIRScanSDK.GetSpectrumCoef();
    }

    public void requestDeviceInfo() {
        ISCNIRScanSDK.GetDeviceInfo();
    }

    public void synchronizeCurrentTime() {
        ISCNIRScanSDK.SetCurrentTime();
    }
}

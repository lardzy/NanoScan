package com.gttcgf.nanoscan.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class SdkBroadcastRegistry {
    public interface BroadcastHandler {
        void onReceive(Context context, Intent intent);
    }

    private static class Registration {
        private final String action;
        private final BroadcastHandler handler;
        private final boolean once;
        private final BroadcastReceiver directReceiver;
        private BroadcastReceiver receiver;

        private Registration(String action, BroadcastHandler handler, boolean once) {
            this.action = action;
            this.handler = handler;
            this.once = once;
            this.directReceiver = null;
        }

        private Registration(String action, BroadcastReceiver directReceiver) {
            this.action = action;
            this.handler = null;
            this.once = false;
            this.directReceiver = directReceiver;
        }
    }

    private final LocalBroadcastManager broadcastManager;
    private final Map<String, Registration> registrations = new LinkedHashMap<>();

    public SdkBroadcastRegistry(Context context) {
        this.broadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
    }

    public void register(String action, BroadcastHandler handler) {
        registrations.put(action, new Registration(action, handler, false));
    }

    public void register(String action, BroadcastReceiver receiver) {
        registrations.put(action, new Registration(action, receiver));
    }

    public void registerOnce(String action, BroadcastHandler handler) {
        registrations.put(action, new Registration(action, handler, true));
    }

    public void registerAll() {
        for (Registration registration : registrations.values()) {
            if (registration.receiver != null) {
                continue;
            }
            registration.receiver = registration.directReceiver != null ? registration.directReceiver : createReceiver(registration);
            broadcastManager.registerReceiver(registration.receiver, new IntentFilter(registration.action));
        }
    }

    public void unregister(String action) {
        Registration registration = registrations.get(action);
        if (registration == null || registration.receiver == null) {
            return;
        }
        broadcastManager.unregisterReceiver(registration.receiver);
        registration.receiver = null;
    }

    public void unregisterAll() {
        for (Registration registration : registrations.values()) {
            if (registration.receiver != null) {
                broadcastManager.unregisterReceiver(registration.receiver);
                registration.receiver = null;
            }
        }
    }

    private BroadcastReceiver createReceiver(Registration registration) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                registration.handler.onReceive(context, intent);
                if (registration.once) {
                    unregister(registration.action);
                }
            }
        };
    }
}

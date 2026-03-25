package com.gttcgf.nanoscan.tools;

import com.gttcgf.nanoscan.R;

public final class DeviceUiUtils {
    private DeviceUiUtils() {
    }

    public static int getBatteryIconRes(int battery) {
        if (battery >= 0 && battery <= 12) {
            return R.drawable.baseline_battery_0_bar_24;
        } else if (battery <= 25) {
            return R.drawable.baseline_battery_1_bar_24;
        } else if (battery <= 37) {
            return R.drawable.baseline_battery_2_bar_24;
        } else if (battery <= 50) {
            return R.drawable.baseline_battery_3_bar_24;
        } else if (battery <= 62) {
            return R.drawable.baseline_battery_4_bar_24;
        } else if (battery <= 75) {
            return R.drawable.baseline_battery_5_bar_24;
        } else if (battery <= 87) {
            return R.drawable.baseline_battery_6_bar_24;
        } else if (battery <= 100) {
            return R.drawable.baseline_battery_full_24;
        }
        return R.drawable.baseline_battery_charging_full_24;
    }
}

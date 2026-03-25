package com.gttcgf.nanoscan.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.gttcgf.nanoscan.LocalReferenceIntensity;
import com.gttcgf.nanoscan.NirSpectralData;
import com.gttcgf.nanoscan.PredictionResultDescription;
import com.gttcgf.nanoscan.R;
import com.gttcgf.nanoscan.tools.SpectralDataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.LinkedHashMap;

public class SpectralRepository {
    private final Context appContext;

    public SpectralRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public LinkedHashMap<String, PredictionResultDescription> loadSpectralIndex(String userPhoneNumber, String deviceMac) {
        SpectralDataUtils.loadSpectralFileMapFromFile(appContext, userPhoneNumber, deviceMac);
        return new LinkedHashMap<>(SpectralDataUtils.userSpectralFileMap);
    }

    public boolean deleteSpectralRecord(String userPhoneNumber, String deviceMac, String fileName) {
        PredictionResultDescription removed = SpectralDataUtils.userSpectralFileMap.remove(fileName);
        if (removed == null) {
            return false;
        }
        boolean mapSaved = SpectralDataUtils.saveSpectrumFileMapToLocal(appContext, userPhoneNumber, deviceMac);
        boolean spectralDeleted = SpectralDataUtils.deleteNirSpectralDataFile(appContext, userPhoneNumber, fileName);
        return mapSaved && spectralDeleted;
    }

    public boolean saveSpectrum(String userPhoneNumber, NirSpectralData nirSpectralData) {
        return SpectralDataUtils.saveSpectrumFileToLocal(appContext, userPhoneNumber, nirSpectralData);
    }

    public LocalReferenceIntensity loadReferenceIntensity(String deviceMac) {
        try (FileInputStream fis = appContext.openFileInput(appContext.getString(R.string.file_localReferenceIntensity, deviceMac));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            LocalReferenceIntensity referenceIntensity = (LocalReferenceIntensity) ois.readObject();
            if (referenceIntensity.getDeviceMAC().equals(deviceMac)) {
                return referenceIntensity;
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
        return null;
    }

    public boolean saveReferenceIntensity(String deviceMac, LocalReferenceIntensity referenceIntensity) {
        try (FileOutputStream fos = appContext.openFileOutput(appContext.getString(R.string.file_localReferenceIntensity, deviceMac), Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(referenceIntensity);
            if (referenceIntensity != null && referenceIntensity.getUpDateTime() != null) {
                String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(referenceIntensity.getUpDateTime());
                SharedPreferences preferences = appContext.getSharedPreferences(deviceMac, Context.MODE_PRIVATE);
                preferences.edit()
                        .putString(appContext.getString(R.string.pref_app_reference_update_time), updateTime)
                        .apply();
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean deleteReferenceIntensity(String deviceMac) {
        File referenceFile = new File(appContext.getFilesDir(), appContext.getString(R.string.file_localReferenceIntensity, deviceMac));
        boolean deleted = referenceFile.delete();
        if (deleted) {
            SharedPreferences preferences = appContext.getSharedPreferences(deviceMac, Context.MODE_PRIVATE);
            preferences.edit().remove(appContext.getString(R.string.pref_app_reference_update_time)).apply();
        }
        return deleted;
    }
}

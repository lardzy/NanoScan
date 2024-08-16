package com.gttcgf.nanoscan.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.NirSpectralData;
import com.gttcgf.nanoscan.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpectralDataUtils {
    private static final String TAG = "SpectralDataUtils";
    public static HashMap<String, String> userSpectralFileMap = new HashMap<>();

    private SpectralDataUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void saveSpectrumFileToLocal(Context context, String userPhoneNumber, NirSpectralData nirSpectralData) {
        // 将光谱数据保存至本地
        if (userPhoneNumber.isEmpty() || nirSpectralData == null) {
            return;
        }

        try {
            // 光谱文件夹路径名称格式为手机号
            File userDir = new File(context.getFilesDir(), userPhoneNumber);
            if (!userDir.exists()) {
                if (!userDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory for user: " + userPhoneNumber);
                    return;
                }
            }
            // 获得光谱文件名
            String fileName = nirSpectralData.getFileNamePrefix();
            if (fileName != null && !fileName.isEmpty()) {
                // 创建光谱file对象，指定路径和文件名
                File file = new File(userDir, fileName + ".ser");
                File userSpectralFileMapFile = new File(context.getFilesDir(), userPhoneNumber + "_"
                        + nirSpectralData.getDeviceMAC() + "_userSpectralFileMap.ser");

                try (FileOutputStream fos = new FileOutputStream(file);
                     FileOutputStream fos_map = new FileOutputStream(userSpectralFileMapFile);
                     ObjectOutputStream oos = new ObjectOutputStream(fos);
                     ObjectOutputStream oos_map = new ObjectOutputStream(fos_map)) {
                    if (!file.exists()) {
                        // 写入光谱序列化对象
                        oos.writeObject(nirSpectralData);
                        // 将光谱用户手机和文件名称保存到map集合中
                        userSpectralFileMap.put(userPhoneNumber, fileName);
                        oos_map.writeObject(userSpectralFileMap);
                        if (file.exists() && userSpectralFileMapFile.exists()) {
                            Log.d(TAG, "saveSpectrumFileToLocal: 光谱保存成功！" + file.getAbsoluteFile());
                        }
                    } else {
                        Log.e(TAG, "saveSpectrumFileToLocal: 存在同名光谱文件", new Exception());
                    }
                } catch (IOException e) {
                    userSpectralFileMap.remove(userPhoneNumber);
                    boolean deleted = file.delete();
                    Log.e(TAG, "saveSpectrumFileToLocal: 保存光谱文件失败,尝试删除光谱、并清除集合数据：" + deleted, e);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving spectrum");
        }
    }

    // 从本地序列化文件读取设备集合对象
    public static List<DeviceItem> readDeviceListFromFile(Context context, String
            userPhoneNumber) {
        // 文件中读取到的所有的设备列表
        List<DeviceItem> loadedDeviceList = new ArrayList<>();
        // 筛选出的当前用户的设备列表
        List<DeviceItem> itemList = new ArrayList<>();

        try (FileInputStream fis = context.openFileInput(context.getString(R.string.file_deviceItem, userPhoneNumber));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Log.d(TAG, "主界面-设备列表文件读取成功！");
            loadedDeviceList = (List<DeviceItem>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "主界面-设备列表文件不存在或读取失败！");
        }
        // 只读取当前用户的设备
        if (!loadedDeviceList.isEmpty()) {
            for (int i = 0; i < loadedDeviceList.size(); i++) {
                DeviceItem deviceItem = loadedDeviceList.get(i);
                if (deviceItem.getUser().equals(userPhoneNumber)) {
                    itemList.add(loadedDeviceList.get(i));
                }
            }
        }
        return itemList;
    }

    //
    public static boolean writeDeviceListToFile(Context context, String
            userPhoneNumber, List<DeviceItem> itemList) {
        try (FileOutputStream fos = context.openFileOutput(context.getString(R.string.file_deviceItem, userPhoneNumber), 0);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(itemList);
            Log.d(TAG, "设备列表界面-设备列表配置文件已写入本地");
        } catch (IOException e) {
            Log.e(TAG, "设备列表界面-配置信息写入失败，请检查设备存储空间！");
            return false;
        }
        return true;
    }

}

package com.gttcgf.nanoscan.tools;

import android.content.Context;
import android.util.Log;

import com.gttcgf.nanoscan.DeviceItem;
import com.gttcgf.nanoscan.NirSpectralData;
import com.gttcgf.nanoscan.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class SpectralDataUtils {
    private static final String TAG = "SpectralDataUtils";
    public static final int STORAGE_MAXIMUM = 10000;
    // Map集合中，键为手机号，值为光谱文件全名
    public static LinkedHashSet<String> userSpectralFileSet = new LinkedHashSet<>();

    private SpectralDataUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // 同时保存光谱文件、光谱索引文件
    public static boolean saveSpectrumFileToLocal(Context context, String userPhoneNumber, NirSpectralData nirSpectralData) {
        // 将光谱数据保存至本地
        if (userPhoneNumber.isEmpty() || nirSpectralData == null) {
            return false;
        }

        try {
            // 光谱文件夹路径名称格式为手机号
            File userDir = new File(context.getFilesDir(), userPhoneNumber);
            if (!userDir.exists()) {
                if (!userDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory for user: " + userPhoneNumber);
                    return false;
                }
                Log.d(TAG, "saveSpectrumFileToLocal: 文件夹创建成功。");
            }
            // 获得光谱文件名
            String fileName = nirSpectralData.getFileNamePrefix();
            if (fileName != null && !fileName.isEmpty()) {
                // 创建光谱file对象，指定路径和文件名
                File spectrumFile = new File(userDir, context.getString(R.string.file_nirSpectralData, fileName));
                File userSpectralFileMapFile = new File(context.getFilesDir(), context.getString(R.string.file_userSpectralFileSet, userPhoneNumber,
                        nirSpectralData.getDeviceMAC()));

                if (!spectrumFile.exists()) {
                    try (FileOutputStream fos = new FileOutputStream(spectrumFile);
                         FileOutputStream fos_map = new FileOutputStream(userSpectralFileMapFile);
                         ObjectOutputStream oos = new ObjectOutputStream(fos);
                         ObjectOutputStream oos_map = new ObjectOutputStream(fos_map)) {
                        // 当达到设定上限
                        if (userSpectralFileSet.size() == STORAGE_MAXIMUM) {
                            // 获得最早元素
                            String earliest = userSpectralFileSet.iterator().next();
                            // 索引集合中删除元素
                            userSpectralFileSet.remove(earliest);
                            // 本地文件中删除该文件
                            File earliestFile = new File(userDir, context.getString(R.string.file_nirSpectralData, earliest));
                            boolean deleted = earliestFile.delete();
                            Log.e(TAG, "saveSpectrumFileToLocal: 由于超出本地保存光谱上限，现删除：" + earliest + "，删除结果：" + deleted);
                        }
                        // 写入光谱序列化对象
                        oos.writeObject(nirSpectralData);
                        // 将光谱文件名称保存到set集合中
                        userSpectralFileSet.add(fileName);
                        // 写入光谱索引序列化对象
                        oos_map.writeObject(userSpectralFileSet);
                        if (spectrumFile.exists() && userSpectralFileMapFile.exists()) {
                            Log.d(TAG, "saveSpectrumFileToLocal: 光谱保存成功！" + spectrumFile.getAbsoluteFile());
                            return true;
                        }
                    } catch (IOException e) {
                        // 出现错误则删除集合中光谱索引、删除本地光谱文件。
                        userSpectralFileSet.remove(fileName);
                        boolean deleted = spectrumFile.delete();
                        Log.e(TAG, "saveSpectrumFileToLocal: 保存光谱文件失败,尝试删除光谱、并清除集合数据：" + deleted, e);
                        return false;
                    }
                } else {
                    Log.e(TAG, "saveSpectrumFileToLocal: 存在同名光谱文件", new Exception());
                    return false;
                }


            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving spectrum");
            return false;
        }
        return false;
    }
    // 从本地反序列化光谱索引集合
    public static void LoadSpectralFileMapFromFile(Context context, String phoneNumber, String deviceMac) {
        if (phoneNumber.isEmpty() || deviceMac.isEmpty()) {
            return;
        }
        try (FileInputStream fis = context.openFileInput(context.getString(R.string.file_userSpectralFileSet, phoneNumber, deviceMac));
             ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            userSpectralFileSet = (LinkedHashSet<String>) ois.readObject();
//            userSpectralFileSet.clear();
//            userSpectralFileSet.putAll((LinkedHashMap<String, String>) ois.readObject());
            Log.d(TAG, "LoadSpectralFileMapFromFile: 读取本地光谱索引成功！");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "LoadSpectralFileMapFromFile: 读取本地光谱索引失败！文件不存在或读取失败");
        }
    }

    // 根据手机号、文件全名，读取红外光谱文件（文件包含图谱、预测结果）
    public static NirSpectralData readNirSpectralDataFromFile(Context context, String userPhoneNumber, String fileName) {
        // 光谱文件夹路径名称格式为手机号
        if (!userPhoneNumber.isEmpty() && !fileName.isEmpty()) {
            File userDir = new File(context.getFilesDir(), userPhoneNumber);
            File NirSpectralDataFile = new File(userDir, context.getString(R.string.file_nirSpectralData, fileName));
            try (
                    FileInputStream fis = new FileInputStream(NirSpectralDataFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                return (NirSpectralData) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "readNirSpectralDataFromFile: 未根据索引找到光谱文件");
                return null;
            }
        } else {
            return null;
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

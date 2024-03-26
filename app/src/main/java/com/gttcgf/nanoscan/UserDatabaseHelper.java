package com.gttcgf.nanoscan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // 数据库版本号
    public static final int DATABASE_VERSION = 1;
    // 数据库名称
    public static final String DATABASE_NAME = "userDatabase.db";

    // 用户表：UserID 主键, PhoneNumber 用户手机号、要求唯一, PasswordHash 用户密码、要求非空, IMEI 设备IMEI号
    // , LoginToken 登录Token, CreateTime, UpdateTime
    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS Users (" +
            "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "PhoneNumber TEXT UNIQUE," +
            "PasswordHash TEXT NOT NULL," +
            "LoginToken TEXT," +
            "IMEI TEXT," +
            "CreateTime TEXT NOT NULL," +
            "UpdateTime TEXT NOT NULL" +
            ")";

    // 设备表：DeviceID, UserID, CustomName, MACAddress, AuthorizationCode, DeviceToken, CreateTime, UpdateTime
    private static final String CREATE_TABLE_DEVICES = "CREATE TABLE IF NOT EXISTS Devices (" +
            "DeviceID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserID INTEGER," +
            "CustomName TEXT," +
            "MACAddress TEXT UNIQUE," +
            "AuthorizationCode TEXT UNIQUE," +
            "DeviceToken TEXT," +
            "CreateTime TEXT NOT NULL," +
            "UpdateTime TEXT NOT NULL," +
            "FOREIGN KEY(UserID) REFERENCES Users(UserID)" +
            ")";


    public UserDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USERS);
        sqLiteDatabase.execSQL(CREATE_TABLE_DEVICES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

package com.gttcgf.nanoscan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // 数据库版本号
    private static final int DATABASE_VERSION = 1;
    // 数据库名称
    private static final String DATABASE_NAME = "userDatabase.db";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS Users (" +
            "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "PhoneNumber TEXT UNIQUE," +
            "PasswordHash TEXT," +
            "LoginToken TEXT" +
            ")";

    private static final String CREATE_TABLE_DEVICES = "CREATE TABLE IF NOT EXISTS Devices (" +
            "DeviceID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserID INTEGER," +
            "CustomName TEXT," +
            "MACAddress TEXT UNIQUE," +
            "AuthorizationCode TEXT UNIQUE," +
            "DeviceToken TEXT," +
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

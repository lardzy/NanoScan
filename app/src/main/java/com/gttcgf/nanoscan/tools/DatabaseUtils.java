package com.gttcgf.nanoscan.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import com.gttcgf.nanoscan.UserDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseUtils {
    private Context context;
    private UserDatabaseHelper userDatabaseHelper;
    private SQLiteDatabase db;

    public DatabaseUtils(Context context) {
        this.context = context;
        this.userDatabaseHelper = new UserDatabaseHelper
                (context, UserDatabaseHelper.DATABASE_NAME, null, UserDatabaseHelper.DATABASE_VERSION);
        this.db = userDatabaseHelper.getWritableDatabase();
    }

    public boolean insertData(ContentValues values) {
        try {
            long newRowId = db.insertOrThrow("Users", null, values);
            // 插入成功，处理成功
            return true;
        } catch (SQLiteException e) {
            // 插入失败，处理错误
            e.printStackTrace();
            Toast.makeText(context, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public Cursor queryData(String table, String[] columns, String selection, String[] selectionArgs) {
        return db.query(table, columns, selection, selectionArgs, null, null, null);
    }

    public int updateData(String table, ContentValues values, String selection, String[] selectionArgs) {
        return db.update(table, values, selection, selectionArgs);
    }

    public int deleteData(String table, String selection, String[] selectionArgs) {
        return db.delete(table, selection, selectionArgs);
    }
}

package liubaoyua.customtext.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import liubaoyua.customtext.entity.AppInfo;

/**
 * Created by liubaoyua on 2015/7/27 0027.
 */
public class DBManager {

    private static DBManager instance;
    private DBHelper helper;
    private SQLiteDatabase db;

    private DBManager(Context context) {
        helper = DBHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
            return instance;
        } else {
            return instance;
        }
    }


    public static void createDateBase(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + "'APP_INFO' (" + //
                    "'PACKAGE_NAME' TEXT PRIMARY KEY ," + // 1: packageName
                    "'APP_NAME' TEXT NOT NULL ," + // 2: appName
                    "'APP_NAME_PIN_YIN' TEXT NOT NULL ," + // 3: appNamePinYin
                    "'APP_NAME_HEAD_CHAR' TEXT NOT NULL ," + // 4: appNameHeadChar
                    "'FIRST_INSTALL_TIME' INTEGER," + // firstInstallTime
                    "'LAST_UPDATE_TIME' INTEGER," + // lastUpdateTime
                    "'STATE' INTEGER );"); // state
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    protected static String[] getColumnNames(SQLiteDatabase db, String tableName) {
        String[] columnNames = null;
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (null != c) {
                int columnIndex = c.getColumnIndex("name");
                if (-1 == columnIndex) {
                    return null;
                }

                int index = 0;
                columnNames = new String[c.getCount()];
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    columnNames[index] = c.getString(columnIndex);
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return columnNames;
    }

    public void insert(AppInfo appInfo) {
        db.execSQL(
                "INSERT INTO " + DBHelper.TABLE_NAME + " VALUES(?,?,?,?,?,?,?)",
                new Object[]{appInfo.packageName, appInfo.appName,
                        appInfo.appNamePinYin, appInfo.appNameHeadChar, appInfo.firstInstallTime,
                        appInfo.lastUpdateTime, appInfo.state});
    }

    public void insert(List<AppInfo> appInfoList) {
        if (appInfoList == null) {
            return;
        }
        for (AppInfo appInfo : appInfoList) {
            insert(appInfo);
        }
    }

    private void updateCustom(AppInfo appInfo) {
        db.execSQL("UPDATE " + DBHelper.TABLE_NAME
                        + " SET "
                        + DBHelper.Properties.state.columnName + " = ? , "
                        + " WHERE " + DBHelper.Properties.packageName.columnName + " = ?",
                new Object[]{appInfo.state, appInfo.packageName});
    }

    public void delete(String packageName) {
        db.execSQL("DELETE FROM " + DBHelper.TABLE_NAME + " WHERE " + DBHelper.Properties.packageName.columnName + " = ? ",
                new String[]{packageName});
    }

    public void delete() {
        db.execSQL("DELETE FROM " + DBHelper.TABLE_NAME);
    }

    public List<AppInfo> query() {
        List<AppInfo> appInfoList = new ArrayList<>();
        Cursor cursor = queryTheCursor();
        AppInfo appInfo;
        while (cursor.moveToNext()) {
            int offset = cursor.getColumnIndex(DBHelper.Properties.packageName.columnName);
            appInfo = new AppInfo( //
                    cursor.getString(offset), // packageName
                    cursor.isNull(offset + 1) ? "" :cursor.getString(offset + 1), // appName
                    cursor.isNull(offset + 2) ? "":cursor.getString(offset + 2), // appNamePinYin
                    cursor.isNull(offset + 3) ? "" :cursor.getString(offset + 3), // appNameHeadChar
                    cursor.isNull(offset + 4) ? 0 : cursor.getLong(offset + 4), // firstInstallTime
                    cursor.isNull(offset + 5) ? 0 : cursor.getLong(offset + 5), // lastUpdateTime
                    cursor.isNull(offset + 6) ? 0 : cursor.getInt(offset + 6) // state
            );
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }

    public Cursor queryTheCursor() {
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, new String[]{});
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        db = null;
        helper.close();
        instance = null;
    }
}

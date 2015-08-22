package liubaoyua.customtext.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by liubaoyua on 2015/7/27 0027.
 */
public class DBHelper extends SQLiteOpenHelper {

    public final static int DATABASE_VERSION = 2;
    public final static String TABLE_NAME = "APP_INFO";
    public final static String DATABASE_NAME = "AppList.db3";
    private static DBHelper mInstance;

    public DBHelper(Context context) {
        super(context, DBHelper.DATABASE_NAME, null, DATABASE_VERSION);
    }

    public synchronized static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
        }
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        DBManager.createDateBase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public static class Properties {
        public final static Property packageName = new Property("packageName", true, "PACKAGE_NAME");
        public final static Property appName = new Property("appName", false, "APP_NAME");
        public final static Property appNamePinyin = new Property("appNamePinYin", false, "APP_NAME_PIN_YIN");
        public final static Property appNameHeadChar = new Property("appNameHeadChar", false, "APP_NAME_HEAD_CHAR");
        public final static Property firstInstallTime = new Property("firstInstallTime", false, "FIRST_INSTALL_TIME");
        public final static Property lastUpdateTime = new Property("lastUpdateTime", false, "LAST_UPDATE_TIME");
        public final static Property state = new Property("state", false, "STATE");
    }

    public static class Property {
        public final String name;
        public final boolean primaryKey;
        public final String columnName;

        public Property(String name, boolean primaryKey, String columnName) {
            this.name = name;
            this.primaryKey = primaryKey;
            this.columnName = columnName;
        }
    }
}

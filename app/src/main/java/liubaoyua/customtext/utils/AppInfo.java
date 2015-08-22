package liubaoyua.customtext.utils;

import android.content.pm.PackageInfo;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 * AppInfo
 */
public class AppInfo {

    public final static int ENABLED = 1;
    public final static int UNKNOWN = 0;
    public final static int DISABLED = -1;

    public String appName = "";

    public String appNamePinyin = "";
    public String appNamePinyinHeadChar = "";
    public String packageName = "";
    public long firstInstallTime = 0L;
    public long lastUpdateTime = 0L;
    public int state = UNKNOWN;

    @Override
    public String toString() {
        return "AppInfoItem{" +
                "appName='" + appName + '\'' +
                ", appNamePinyin='" + appNamePinyin + '\'' +
                ", appNamePinyinHeadChar='" + appNamePinyinHeadChar + '\'' +
                ", packageName='" + packageName + '\'' +
                ", firstInstallTime=" + firstInstallTime +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }

    public AppInfo(PackageInfo packageInfo, String appName){
        this.appName = appName;
        this.packageName = packageInfo.packageName;
        this.firstInstallTime = packageInfo.firstInstallTime;
        this.lastUpdateTime = packageInfo.lastUpdateTime;
        this.appNamePinyin = Utils.getPinYin(appName.toLowerCase());
        this.appNamePinyinHeadChar = Utils.getPinYinHeadChar(appName.toLowerCase());
    }

    public AppInfo(PackageInfo packageInfo, String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
        this.firstInstallTime = packageInfo.firstInstallTime;
        this.lastUpdateTime = packageInfo.lastUpdateTime;
        this.appNamePinyin = Utils.getPinYin(appName.toLowerCase());
        this.appNamePinyinHeadChar = Utils.getPinYinHeadChar(appName.toLowerCase());
    }
}

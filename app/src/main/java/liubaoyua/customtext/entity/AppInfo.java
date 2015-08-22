package liubaoyua.customtext.entity;

import android.content.pm.PackageInfo;

import liubaoyua.customtext.utils.Utils;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 * AppInfo
 */
public class AppInfo {

    public final static int ENABLED = 1;
    public final static int UNKNOWN = 0;
    public final static int DISABLED = -1;

    public String packageName = "";
    public String appName = "";
    public String appNamePinYin = "";
    public String appNameHeadChar = "";
    public long firstInstallTime = 0L;
    public long lastUpdateTime = 0L;
    public int state = UNKNOWN;

    @Override
    public String toString() {
        return "AppInfoItem{" +
                "appName='" + appName + '\'' +
                ", appNamePinYin='" + appNamePinYin + '\'' +
                ", appNameHeadChar='" + appNameHeadChar + '\'' +
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
        this.appNamePinYin = Utils.getPinYin(appName.toLowerCase());
        this.appNameHeadChar = Utils.getPinYinHeadChar(appName.toLowerCase());
    }

    public AppInfo(PackageInfo packageInfo, String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
        this.firstInstallTime = packageInfo.firstInstallTime;
        this.lastUpdateTime = packageInfo.lastUpdateTime;
        this.appNamePinYin = Utils.getPinYin(appName.toLowerCase());
        this.appNameHeadChar = Utils.getPinYinHeadChar(appName.toLowerCase());
    }

    public AppInfo(String packageName, String appName, String appNamePinYin,
                   String appNameHeadChar, long firstInstallTime, long lastUpdateTime, int state) {
        this.packageName = packageName;
        this.appName = appName;
        this.appNamePinYin = appNamePinYin;
        this.appNameHeadChar = appNameHeadChar;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.state = state;
    }

}

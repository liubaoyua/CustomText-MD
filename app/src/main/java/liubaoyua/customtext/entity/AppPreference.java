package liubaoyua.customtext.entity;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import liubaoyua.customtext.app.AppHelper;
import liubaoyua.customtext.utils.Common;

/**
 * Created by liubaoyua on 2016/4/28.
 */
public class AppPreference {

    // appPrefs 用于当前 packageName 所在包的数据读写， globalPrefs 是全局信息
    private SharedPreferences appPrefs, globalPrefs;
    private int maxPage = 0;
    private boolean hookEnable;

    private boolean useRegex;
    private boolean moreType;

    private List<CustomText> data;
    private String packageName;

    public AppPreference(String packageName) {
        Context context = AppHelper.getApplication();
        this.packageName =  packageName;
        globalPrefs = context.getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE);
        appPrefs = context.getSharedPreferences(packageName, Context.MODE_WORLD_READABLE);
        maxPage = appPrefs.getInt(Common.MAX_PAGE_OLD, 0);

        int count = (maxPage + 1) * Common.DEFAULT_NUM;

        data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String oriText = appPrefs.getString(Common.ORI_TEXT_PREFIX + i, "");
            String newText = appPrefs.getString(Common.NEW_TEXT_PREFIX + i, "");
            CustomText customText = new CustomText(oriText, newText);
            data.add(customText);
        }
        hookEnable = globalPrefs.getBoolean(packageName, false);

        if (appPrefs.contains(Common.SETTING_MORE_TYPE)) {
            moreType = appPrefs.getBoolean(Common.SETTING_MORE_TYPE, false);
        } else {
            moreType = globalPrefs.getBoolean(Common.SETTING_MORE_TYPE, false);
        }

        if (appPrefs.contains(Common.SETTING_USE_REGEX)) {
            useRegex = appPrefs.getBoolean(Common.SETTING_USE_REGEX, false);
        } else {
            useRegex = globalPrefs.getBoolean(Common.SETTING_USE_REGEX, false);
        }

    }

    public ArrayList<CustomText> getClonedData() {
        ArrayList<CustomText> texts = new ArrayList<>();
        for (CustomText text : data) {
            texts.add(new CustomText(text));
        }
        return texts;
    }

    public boolean isHookEnable() {
        return hookEnable;
    }

    public void setHookEnable(boolean hookEnable) {
        this.hookEnable = hookEnable;
        globalPrefs.edit().putBoolean(packageName, hookEnable).commit();
    }

    public void removeEmptyItems() {
        int count = (maxPage + 1) * Common.DEFAULT_NUM;

        SharedPreferences.Editor editor = appPrefs.edit();
        for (int i = 0; i < count; i++) {
            if (appPrefs.contains(Common.ORI_TEXT_PREFIX + i)) {
                if (appPrefs.getString(Common.ORI_TEXT_PREFIX + i, "").equals(""))
                    editor.remove(Common.ORI_TEXT_PREFIX + i);
            }
            if (appPrefs.contains(Common.NEW_TEXT_PREFIX + i)) {
                if (appPrefs.getString(Common.NEW_TEXT_PREFIX + i, "").equals(""))
                    editor.remove(Common.NEW_TEXT_PREFIX + i);
            }
        }
        editor.commit();
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public void setMoreTypeEnable(boolean moreType) {
        this.moreType = moreType;
        appPrefs.edit().putBoolean(Common.SETTING_MORE_TYPE, moreType).commit();
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
        appPrefs.edit().putBoolean(Common.SETTING_USE_REGEX, moreType).commit();
    }

    public boolean isMoreTypeEnabled() {
        return moreType;
    }

    public boolean isDataEquals(List<CustomText> texts) {
        return data.equals(texts);
    }

    public void setNewData(List<CustomText> newData) {
        data.clear();
        for (int i = 0; i < newData.size(); i++) {
            data.add(new CustomText(newData.get(i)));
        }
        SharedPreferences.Editor mEditor = appPrefs.edit();

        int all = (maxPage + 1) * Common.DEFAULT_NUM;

        for (int i = 0; i < newData.size(); i++) {
            CustomText temp = newData.get(i);
            if (!temp.oriText.isEmpty()) {
                mEditor.putString(Common.ORI_TEXT_PREFIX + i, temp.oriText);
            } else {
                mEditor.remove(Common.ORI_TEXT_PREFIX + i);
            }

            if (!temp.newText.isEmpty()) {
                mEditor.putString(Common.NEW_TEXT_PREFIX + i, temp.newText);
            } else {
                mEditor.remove(Common.NEW_TEXT_PREFIX + i);
            }
        }

        for (int i = newData.size(); i < all; i++) {
            mEditor.remove(Common.ORI_TEXT_PREFIX + i);
            mEditor.remove(Common.NEW_TEXT_PREFIX + i);
        }

        int delta = newData.size() - newData.size() / Common.DEFAULT_NUM * Common.DEFAULT_NUM == 0 ? 0 : 1;
        int pageNum = newData.size() / Common.DEFAULT_NUM + delta;
        mEditor.putInt(Common.MAX_PAGE_OLD, pageNum - 1);
        mEditor.commit();
    }
}

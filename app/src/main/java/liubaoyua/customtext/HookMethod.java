package liubaoyua.customtext;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import liubaoyua.customtext.entity.CustomText;
import liubaoyua.customtext.utils.Common;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class HookMethod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private XSharedPreferences prefs;
    private Html.ImageGetter imageGetter = new Html.ImageGetter(){
        public Drawable getDrawable(String source){
            Drawable d=Drawable.createFromPath(source);
            d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            return d;
        }
    };

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences(Common.PACKAGE_NAME);
        prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {


        prefs.reload();
        if (!prefs.getBoolean(Common.SETTING_MODULE_SWITCH, true)) {
            return;
        }

        XSharedPreferences mPrefs = new XSharedPreferences(Common.PACKAGE_NAME, lpparam.packageName);
        mPrefs.makeWorldReadable();
        XSharedPreferences sPrefs = new XSharedPreferences(Common.PACKAGE_NAME, Common.SHARING_SETTING_PACKAGE_NAME);
        sPrefs.makeWorldReadable();

        boolean isInDebugMode = prefs.getBoolean(Common.SETTING_XPOSED_DEBUG_MODE,Common.XPOSED_DEBUG);

        final boolean shouldHackMoreType;
        if(mPrefs.contains(Common.SETTING_MORE_TYPE)){
            shouldHackMoreType = mPrefs.getBoolean(Common.SETTING_MORE_TYPE,false);
        }else {
            shouldHackMoreType = prefs.getBoolean(Common.SETTING_MORE_TYPE,false);
        }

        final boolean shouldUseRegex;
        if(mPrefs.contains(Common.SETTING_MORE_TYPE)){
            shouldUseRegex = mPrefs.getBoolean(Common.SETTING_USE_REGEX,true);
        }else {
            shouldUseRegex = prefs.getBoolean(Common.SETTING_USE_REGEX,true);
        }

        final boolean isGlobalHackEnabled = prefs.getBoolean(Common.PREFS, false);
        final boolean isCurrentHackEnabled = prefs.getBoolean(lpparam.packageName, false);
        final boolean isInThisApp = lpparam.packageName.equals(Common.PACKAGE_NAME);
        final boolean isSharedHackEnabled = prefs.getBoolean(Common.SHARING_SETTING_PACKAGE_NAME, false);

        final String thisAppName = prefs.getString(Common.PACKAGE_NAME_ARG, Common.DEFAULT_APP_NAME);

        if (isInDebugMode) {
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   isGlobalHackEnabled： "  + isGlobalHackEnabled);
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   isCurrentHackEnabled： "  + isCurrentHackEnabled);
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   isInThisApp： "  + isInThisApp);
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   isSharedHackEnabled： "  + isSharedHackEnabled);
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   shouldUseRegex： "  + shouldUseRegex);
            XposedBridge.log(thisAppName + ": in " + lpparam.packageName + "   shouldHackMoreType： "  + shouldHackMoreType);
        }

        XC_MethodHook textMethodHook;
        if (isInThisApp) {
            final String hackSucceedMessage;
            String temp = prefs.getString(Common.SETTING_HACK_SUCCEED_MESSAGE, Common.DEFAULT_MESSAGE);

            if (temp.equals("")){
                hackSucceedMessage = prefs.getString(Common.MESSAGE, Common.DEFAULT_MESSAGE);
            }else{
                hackSucceedMessage = temp;
            }

            textMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (methodHookParam.args[0] instanceof String){
                        String abc = (String) methodHookParam.args[0];
                        abc = abc.replaceAll(thisAppName, hackSucceedMessage);
                        methodHookParam.args[0] = abc;
                    }
                }
            };
        } else {
            if (!isGlobalHackEnabled && !isCurrentHackEnabled)
                return;

            if(shouldUseRegex){
                final PatternText[] current = loadPatternTextArrayFromPrefs(mPrefs,isCurrentHackEnabled);
                final PatternText[] shared = loadPatternTextArrayFromPrefs(sPrefs, isSharedHackEnabled && isCurrentHackEnabled);
                final PatternText[] global = loadPatternTextArrayFromPrefs(prefs, isGlobalHackEnabled);

                if (isInDebugMode) {
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(current));
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(shared));
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(global));
                }

                textMethodHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        String abc;
                        if(shouldHackMoreType &&  methodHookParam.args[0] != null ) {
                            abc = methodHookParam.args[0].toString();
                        }else if (!shouldHackMoreType && methodHookParam.args[0] instanceof String) {
                            abc = (String)methodHookParam.args[0];
                        }else {
                            return;
                        }
                        if (isCurrentHackEnabled) {
                            abc = replaceAllFromArray(current, abc);
                        }
                        if(isSharedHackEnabled && isCurrentHackEnabled){
                            abc = replaceAllFromArray(shared, abc);
                        }
                        if (isGlobalHackEnabled) {
                            abc = replaceAllFromArray(global, abc);
                        }
                        setTextFromHtml(abc,methodHookParam,0);
                    }
                };
            }else {
                final CustomText[] current = loadCustomTextArrayFromPrefs(mPrefs, isCurrentHackEnabled);
                final CustomText[] shared = loadCustomTextArrayFromPrefs(sPrefs, isSharedHackEnabled && isCurrentHackEnabled);
                final CustomText[] global = loadCustomTextArrayFromPrefs(prefs, isGlobalHackEnabled);

                if (isInDebugMode) {
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(current));
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(shared));
                    XposedBridge.log(thisAppName + ": in " + lpparam.packageName + Arrays.toString(global));
                }

                textMethodHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                        String abc;
                        if(shouldHackMoreType &&  methodHookParam.args[0] != null ) {
                            abc = methodHookParam.args[0].toString();
                        }else if (!shouldHackMoreType && methodHookParam.args[0] instanceof String) {
                            abc = methodHookParam.args[0].toString();
                        }else {
                            return;
                        }

                        if (isCurrentHackEnabled) {
                            abc = replaceAllFromArray(current, abc);
                        }
                        if(isSharedHackEnabled && isCurrentHackEnabled){
                            abc = replaceAllFromArray(shared, abc);
                        }
                        if (isGlobalHackEnabled) {
                            abc = replaceAllFromArray(global, abc);
                        }
                        setTextFromHtml(abc,methodHookParam,0);

                    }
                };
            }
        }

        findAndHookMethod(TextView.class, "setText", CharSequence.class,
                TextView.BufferType.class, boolean.class, int.class, textMethodHook);
        findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
        findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,
                float.class, float.class, Paint.class, textMethodHook);

        if (isInDebugMode) {
            XposedBridge.log(thisAppName + ":  findAndHookDone");
        }
    }

    // static method

    private void setTextFromHtml(String html,XC_MethodHook.MethodHookParam param,int n) {
        if (html.contains("<")  && html.contains(">")) {
            CharSequence text = Html.fromHtml(html, imageGetter, null);
            if (param.thisObject instanceof TextView)
                param.args[n] = text;
            else
                param.args[n] = text.toString();
        } else
            param.args[n] = html;
    }

    @Deprecated
    public static String replaceAllFromList(List<CustomText> texts, String abc,boolean useRegex){
        for (int i = 0; i < texts.size(); i++) {
            CustomText customText = texts.get(i);
            if(useRegex){
                abc = abc.replaceAll(customText.oriText, customText.newText);
            }else {
                abc = abc.replace(customText.oriText,customText.newText);
            }

        }
        return abc;
    }

    @Deprecated
    public static ArrayList<CustomText> loadListFromPrefs(XSharedPreferences prefs, Boolean enabled){
        if (!enabled){
            return new ArrayList<>();
        }
        ArrayList<CustomText> list = new ArrayList<>();
        final int num = (prefs.getInt(Common.MAX_PAGE_OLD, 0) + 1) * Common.DEFAULT_NUM;
        for (int i = 0; i < num; i++) {
            String oriString = prefs.getString(Common.ORI_TEXT_PREFIX + i, "");
            String newString = prefs.getString(Common.NEW_TEXT_PREFIX + i, "");
            CustomText customText = new CustomText(oriString, newString);
            list.add(customText);
        }
        list = trimTextList(list);
        return list;
    }

    public static Html.ImageGetter getImageGetter(final int picMagnification){
        return new Html.ImageGetter(){
            public Drawable getDrawable(String source){
                Drawable d=Drawable.createFromPath(source);
                d.setBounds(0,0,d.getIntrinsicWidth() * picMagnification
                        ,d.getIntrinsicHeight()* picMagnification);
                return d;
            }
        };
    }

    @Deprecated
    public static ArrayList<CustomText> trimTextList(ArrayList<CustomText> list){
        for(int i = 0; i < list.size(); i++){
            CustomText text = list.get(i);
            if(TextUtils.isEmpty(text.oriText)){
                list.remove(i);
                i--;
            }
        }
        return list;
    }

    public static PatternText[] getPatternsFromList(List<CustomText> list){
        PatternText[] patternTexts = new PatternText[list.size()];
        for(int i = 0 ; i < list.size(); i++){
            CustomText text = list.get(i);
            patternTexts[i] = new PatternText(Pattern.compile(text.oriText),text.newText);
        }
        return patternTexts;
    }

    public static String replaceAllFromArray(PatternText[] patternTexts, String abc){
        if(patternTexts == null){
            return abc;
        }
        for(PatternText patternText:patternTexts){
            abc = patternText.pattern.matcher(abc).replaceAll(patternText.newText);
        }
        return abc;
    }

    public static String replaceAllFromArray(CustomText[] customTexts, String abc){
        if(customTexts == null){
            return abc;
        }
        for(CustomText customText:customTexts){
            abc = abc.replace(customText.oriText, customText.newText);
        }
        return abc;
    }

    public static PatternText[] loadPatternTextArrayFromPrefs(XSharedPreferences prefs, Boolean enabled){
        if (!enabled){
            return null;
        }
        List<PatternText> list = new ArrayList<>();
        final int num = (prefs.getInt(Common.MAX_PAGE_OLD, 0) + 1) * Common.DEFAULT_NUM;
        for (int i = 0; i < num; i++) {
            String oriString = prefs.getString(Common.ORI_TEXT_PREFIX + i, "");
            String newString = prefs.getString(Common.NEW_TEXT_PREFIX + i, "");
            if(!TextUtils.isEmpty(oriString)){
                PatternText patternText = new PatternText(Pattern.compile(oriString), newString);
                list.add(patternText);
            }
        }
        return list.toArray(new PatternText[1]);
    }

    public static CustomText[] loadCustomTextArrayFromPrefs(XSharedPreferences prefs, Boolean enabled){
        if (!enabled){
            return null;
        }
        List<CustomText> list = new ArrayList<>();
        final int num = (prefs.getInt(Common.MAX_PAGE_OLD, 0) + 1) * Common.DEFAULT_NUM;
        for (int i = 0; i < num; i++) {
            String oriString = prefs.getString(Common.ORI_TEXT_PREFIX + i, "");
            String newString = prefs.getString(Common.NEW_TEXT_PREFIX + i, "");
            if(!TextUtils.isEmpty(oriString)){
                CustomText customText = new CustomText(oriString, newString);
                list.add(customText);
            }
        }
        return list.toArray(new CustomText[1]);
    }

    static class PatternText{
        public Pattern pattern;
        public String newText;

        public PatternText(Pattern pattern, String newText) {
            this.pattern = pattern;
            this.newText = newText;
        }

        @Override
        public String toString() {
            return "PatternText{" +
                    "pattern=" + pattern +
                    ", newText='" + newText + '\'' +
                    '}';
        }
    }
}
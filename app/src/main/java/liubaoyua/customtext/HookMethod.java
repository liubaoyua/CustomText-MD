package liubaoyua.customtext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import liubaoyua.customtext.entity.CustomText;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.XposedUtil;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class HookMethod implements IXposedHookLoadPackage {

    private XSharedPreferences prefs;
    private CustomText[] current;
    private CustomText[] shared;
    private CustomText[] global;
    private Html.ImageGetter imageGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {
            Drawable d = Drawable.createFromPath(source);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    };

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        prefs = new XSharedPreferences(Common.PACKAGE_NAME);
        prefs.makeWorldReadable();
        if (!prefs.getBoolean(Common.SETTING_MODULE_SWITCH, true)) {
            return;
        }

        XSharedPreferences mPrefs = new XSharedPreferences(Common.PACKAGE_NAME, lpparam.packageName);
        mPrefs.makeWorldReadable();
        XSharedPreferences sPrefs = new XSharedPreferences(Common.PACKAGE_NAME, Common.SHARING_SETTING_PACKAGE_NAME);
        sPrefs.makeWorldReadable();

        final boolean isInDebugMode = prefs.getBoolean(Common.SETTING_XPOSED_DEBUG_MODE, Common.XPOSED_DEBUG);

        final boolean shouldHackMoreType;
        if (mPrefs.contains(Common.SETTING_MORE_TYPE)) {
            shouldHackMoreType = mPrefs.getBoolean(Common.SETTING_MORE_TYPE, false);
        } else {
            shouldHackMoreType = prefs.getBoolean(Common.SETTING_MORE_TYPE, false);
        }

        final boolean shouldUseRegex;
        if (mPrefs.contains(Common.SETTING_MORE_TYPE)) {
            shouldUseRegex = mPrefs.getBoolean(Common.SETTING_USE_REGEX, false);
        } else {
            shouldUseRegex = prefs.getBoolean(Common.SETTING_USE_REGEX, false);
        }

        final boolean isGlobalHackEnabled = prefs.getBoolean(Common.PREFS, false);
        final boolean isCurrentHackEnabled = prefs.getBoolean(lpparam.packageName, false);
        final boolean isInThisApp = lpparam.packageName.equals(Common.PACKAGE_NAME);
        final boolean isSharedHackEnabled = prefs.getBoolean(Common.SHARING_SETTING_PACKAGE_NAME, false);

        final String thisAppName = prefs.getString(Common.PACKAGE_NAME_ARG, Common.DEFAULT_APP_NAME);

        if (isInDebugMode) {
            JSONObject object = new JSONObject();
            XposedBridge.log("Custom Text Debugging...");
            object.put("packageName", lpparam.packageName)
                    .put("isGlobalHackEnabled", isGlobalHackEnabled)
                    .put("isCurrentHackEnabled", isCurrentHackEnabled)
                    .put("isSharedHackEnabled", isSharedHackEnabled)
                    .put("shouldUseRegex", shouldUseRegex)
                    .put("shouldHackMoreType", shouldHackMoreType);
            try {
                XposedBridge.log(object.toString(4));
            } catch (Throwable e) {
            }
        }

        XC_MethodHook textMethodHook;
        if (isInThisApp) {
            final String hackSucceedMessage;
            String temp = prefs.getString(Common.SETTING_HACK_SUCCEED_MESSAGE, Common.DEFAULT_MESSAGE);

            if (temp.equals("")) {
                hackSucceedMessage = prefs.getString(Common.MESSAGE, Common.DEFAULT_MESSAGE);
            } else {
                hackSucceedMessage = temp;
            }

            textMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (methodHookParam.args[0] instanceof String) {
                        String abc = (String) methodHookParam.args[0];
                        abc = abc.replaceAll(thisAppName, hackSucceedMessage);
                        methodHookParam.args[0] = abc;
                    }
                }
            };
            findAndHookMethod(XposedUtil.class.getName(), lpparam.classLoader,
                    "isXposedEnable", XC_MethodReplacement.returnConstant(true));
        } else {
            if (!isGlobalHackEnabled && !isCurrentHackEnabled)
                return;

            current = loadCustomTextArrayFromPrefs(mPrefs, isCurrentHackEnabled, shouldUseRegex);
            shared = loadCustomTextArrayFromPrefs(sPrefs, isSharedHackEnabled && isCurrentHackEnabled, shouldUseRegex);
            global = loadCustomTextArrayFromPrefs(prefs, isGlobalHackEnabled, shouldUseRegex);

            if (isInDebugMode) {
                try {
                    JSONObject object = new JSONObject();
                    XposedBridge.log("Custom Text pending replacement...");
                    object.put("packageName", lpparam.packageName)
                            .put("current", toJSonArray(current))
                            .put("shared", toJSonArray(shared))
                            .put("global", toJSonArray(global));

                    XposedBridge.log(object.toString(4));
                } catch (Throwable e) {
                }
            }

            textMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    String source;
                    if (shouldHackMoreType && methodHookParam.args[0] != null) {
                        source = methodHookParam.args[0].toString();
                    } else if (!shouldHackMoreType && methodHookParam.args[0] instanceof String) {
                        source = (String) methodHookParam.args[0];
                    } else {
                        return;
                    }

                    Result result = new Result(source, false);
                    if (isCurrentHackEnabled) {
                        replaceAllFromArray(current, result, shouldUseRegex);
                    }
                    if (isSharedHackEnabled && isCurrentHackEnabled) {
                        replaceAllFromArray(shared, result, shouldUseRegex);
                    }
                    if (isGlobalHackEnabled) {
                        replaceAllFromArray(global, result, shouldUseRegex);
                    }

                    if (result.isChange()) {
                        // changed... so we reset the arg[0]...
                        setTextFromHtml(result.getText(), methodHookParam, 0);
                        if (isInDebugMode) {
                            try {
                                JSONObject o = new JSONObject();
                                o.put("package", lpparam.packageName)
                                        .put("source", source)
                                        .put("after", result.getText());
                                XposedBridge.log("Custom Text... Replace text in Package: \n" + o.toString(4));
                            } catch (Throwable e) {
                                //
                            }
                        }
                    }
                }
            };
        }

        findAndHookMethod(TextView.class, "setText", CharSequence.class,
                TextView.BufferType.class, boolean.class, int.class, textMethodHook);
        findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
        try {
            findAndHookMethod(Canvas.class, "drawText", String.class,
                    float.class, float.class, Paint.class, textMethodHook);
        } catch (Throwable e) {
            //ignore...
        }

        if (isInDebugMode) {
            XposedBridge.log("Custom Text..." + thisAppName + ":  findAndHookDone");
        }
    }

    private void setTextFromHtml(String html, XC_MethodHook.MethodHookParam param, int n) {
        if (html.contains("<") && html.contains(">")) {
            CharSequence text = Html.fromHtml(html, imageGetter, null);
            if (param.thisObject instanceof TextView)
                param.args[n] = text;
            else
                param.args[n] = text.toString();
        } else
            param.args[n] = html;
    }

    public static void replaceAllFromArray(CustomText[] customTexts, Result result, boolean regex) {
        if (customTexts == null) {
            return;
        }
        for (CustomText customText : customTexts) {
            if (regex) {
                result = patternReplace(result, customText.getPattern(), customText.getNewText());
            } else {
                result = normalReplace(result, customText.getOriText(), customText.getNewText());
            }
        }
    }

    public static JSONArray toJSonArray(CustomText[] texts) throws JSONException {
        if (texts == null || texts.length == 0) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (CustomText text : texts) {
            array.put(text.toJson());
        }
        return array;
    }

    public static CustomText[] loadCustomTextArrayFromPrefs(XSharedPreferences prefs, Boolean enabled, boolean shouldUseRegex) {
        if (!enabled) {
            return null;
        }
        List<CustomText> list = new ArrayList<>();
        final int num = (prefs.getInt(Common.MAX_PAGE_OLD, 0) + 1) * Common.DEFAULT_NUM;
        for (int i = 0; i < num; i++) {
            String oriString = prefs.getString(Common.ORI_TEXT_PREFIX + i, "");
            String newString = prefs.getString(Common.NEW_TEXT_PREFIX + i, "");
            if (!TextUtils.isEmpty(oriString)) {
                CustomText customText = new CustomText(oriString, newString);
                if (!shouldUseRegex) {
                    list.add(customText);
                } else if (customText.createOriginPattern() != null) {
                    list.add(customText);
                }
            }
        }
        return list.toArray(new CustomText[list.size()]);
    }


    public static Result patternReplace(Result origin, Pattern target, String replacement) {
        String text = origin.getText();
        Matcher matcher = target.matcher(text);
        StringBuffer buffer = null;
        boolean change = false;
        matcher.reset();
        while (matcher.find()) {
            if (buffer == null) {
                buffer = new StringBuffer(text.length());
            }
            matcher.appendReplacement(buffer, replacement);
            change = true;
        }
        if (change) {
            origin.setText(matcher.appendTail(buffer).toString());
        }
        return origin;
    }

    @SuppressWarnings("all")
    public static Result normalReplace(Result origin, CharSequence target, CharSequence replacement) {
        String text = origin.getText();
        String result = text.replace(target, replacement);
        // if text not change, the method "String.replace(target, replacement)" will return text itself.
        // so just check text != result is ok...
        if (text != result) {
            origin.setText(result);
        }
        return origin;
    }

    /**
     * Created by liubaoyua on 2016/4/21.
     */
    static class Result {
        private boolean change;
        private String text;

        public Result(String text, boolean change) {
            this.text = text;
            this.change = change;
        }

        public boolean isChange() {
            return change;
        }

        public void setText(String text) {
            this.text = text;
            this.change = true;
        }

        public String getText() {
            return text;
        }
    }
}
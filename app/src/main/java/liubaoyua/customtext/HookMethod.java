package liubaoyua.customtext;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.CustomText;
import liubaoyua.customtext.utils.Utils;


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

        final boolean isInDebugMode = prefs.getBoolean(Common.SETTING_XPOSED_DEBUG_MODE,Common.XPOSED_DEBUG);
        final boolean isHackFurtherEnabled = prefs.getBoolean(Common.SETTING_HACK_FURTHER, true);
        final boolean isGlobalHackEnabled = prefs.getBoolean(Common.PREFS, false);
        final boolean isCurrentHackEnabled = prefs.getBoolean(lpparam.packageName, false);
        final boolean isInThisApp = lpparam.packageName.equals(Common.PACKAGE_NAME);
        final boolean isSharedHackEnabled = prefs.getBoolean(Common.SHARING_SETTING_PACKAGE_NAME,false);

        if (isInDebugMode) {
            XposedBridge.log(Common.PACKAGE_NAME + " : now you are in " + lpparam.packageName);
        }

        XC_MethodHook textMethodHook;
        if (isInThisApp) {
            final String hackSucceedMessage;
            final String thisAppName = prefs.getString(Common.PACKAGE_NAME_ARG, Common.DEFAULT_APP_NAME);
            String temp = prefs.getString(Common.SETTING_HACK_SUCCEED_MESSAGE, Common.DEFAULT_MESSAGE);

            if ( temp == null || temp.equals("")){
                hackSucceedMessage = prefs.getString(Common.MESSAGE, Common.DEFAULT_MESSAGE);
            }else{
                hackSucceedMessage = temp;
            }

            if (thisAppName == null || hackSucceedMessage == null) {
                if (Common.XPOSED_DEBUG)
                    XposedBridge.log("thisAppName or hackSucceedMessage == NULL");
                return;
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
            if (!(isGlobalHackEnabled || !isCurrentHackEnabled || isSharedHackEnabled))
                return;

            final ArrayList<CustomText> currentTexts = Utils.loadListFromPrefs(mPrefs,isCurrentHackEnabled);
            final ArrayList<CustomText> sharedTexts = Utils.loadListFromPrefs(sPrefs,isSharedHackEnabled);
            final ArrayList<CustomText> globalTexts = Utils.loadListFromPrefs(prefs,isGlobalHackEnabled);

            if (isInDebugMode) {
                XposedBridge.log(Common.PACKAGE_NAME + currentTexts.toString());
                XposedBridge.log(Common.PACKAGE_NAME + sharedTexts.toString());
                XposedBridge.log(Common.PACKAGE_NAME + globalTexts.toString());
            }


            if (isHackFurtherEnabled) {
                textMethodHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        CharSequence actualText = (CharSequence) methodHookParam.args[0];
                        if (actualText != null) {
                            String abc = actualText.toString();
                            if (isCurrentHackEnabled) {
                                abc = Utils.replaceAllFromList(currentTexts,abc);
                            }
                            if(isSharedHackEnabled && isCurrentHackEnabled){
                                abc = Utils.replaceAllFromList(sharedTexts,abc);
                            }
                            if (isGlobalHackEnabled) {
                                abc = Utils.replaceAllFromList(globalTexts,abc);
                            }
                            setTextFromHtml(abc,methodHookParam,0);
                        }
                    }
                };
            } else {
                textMethodHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        if (methodHookParam.args[0] instanceof String) {
                            String abc = (String) methodHookParam.args[0];
                            if (isCurrentHackEnabled) {
                                abc = Utils.replaceAllFromList(currentTexts,abc);
                            }
                            if(isSharedHackEnabled && isCurrentHackEnabled){
                                abc = Utils.replaceAllFromList(sharedTexts,abc);
                            }
                            if (isGlobalHackEnabled) {
                                abc = Utils.replaceAllFromList(globalTexts,abc);
                            }
                            setTextFromHtml(abc,methodHookParam,0);
                        }
                    }
                };
            }
        }

        findAndHookMethod(TextView.class, "setText", CharSequence.class,
                TextView.BufferType.class, boolean.class, int.class, textMethodHook);
        findAndHookMethod(TextView.class, "setHint", CharSequence.class, textMethodHook);
        findAndHookMethod("android.view.GLES20Canvas", null, "drawText", String.class,
                float.class, float.class, Paint.class, textMethodHook);

        if(isInDebugMode){
            XposedBridge.log(Common.DEFAULT_APP_NAME + ":  findAndHookDone");
        }
    }

    private void setTextFromHtml(String html,XC_MethodHook.MethodHookParam param,int n) {
        if (html.indexOf("<") != -1 && html.indexOf(">") != -1) {
            CharSequence text = Html.fromHtml(html, imageGetter, null);
            if (param.thisObject instanceof TextView)
                param.args[n] = text;
            else param.args[n] = text.toString();
        } else param.args[n] = html;
    }
}
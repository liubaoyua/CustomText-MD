package liubaoyua.customtext.utils;

/**
 * Created by liubaoyua on 2015/6/19 0019.
 * constants
 */
public class Common {
    public static Boolean DEBUG = false;
    public static Boolean FAST_DEBUG = false;
    public static Boolean XPOSED_DEBUG = true;

    public static String TAG = "liu_bao_yua";
    public final static String POSITION_ARG = "position";
    public final static String PACKAGE_NAME_ARG = "packageName";
    public final static String IS_ENABLED_ARG = "isEnabled";
    public final static String PACKAGE_NAME ="liubaoyua.customtext";
    public final static String SYSTEM_UI_PACKAGE_NAME ="com.android.systemui";
    public final static String GLOBAL_SETTING_PACKAGE_NAME ="liubaoyua.customtext_preferences";
    public final static String SHARING_SETTING_PACKAGE_NAME ="liubaoyua.customtext_sharing_preferences";
    public static String BACKUP_DIR = "/Custom Text";

    public final static String MAX_PAGE_OLD = "maxpage";
    public final static String ORI_TEXT_PREFIX = "oristr";
    public final static String NEW_TEXT_PREFIX = "newstr";

    public final static String PREFS = "liubaoyua.customtext_preferences";

    public final static String MESSAGE = "defaultMessage";
    public final static String PACKAGE_VERSION_CODE="versionCode";
    public final static String DEFAULT_MESSAGE = "模块可用~";
    public final static String DEFAULT_APP_NAME = "^文本自定义$";
    public final static String CHECK_UPDATE = "checkUpdate";

    // in the setting page
    public final static String SETTING_ATTENTION = "attention";
    public final static String SETTING_XPOSED_DEBUG_MODE = "xposedDebug";
    public final static String SETTING_MODULE_SWITCH = "moduleswitch";
    public final static String SETTING_HACK_FURTHER = "ImageText";
    public final static String SETTING_HACK_SUCCEED_MESSAGE = "hackSucceedMessage";


    public static int DEFAULT_NUM = 10 ;
    public static int APP_REQUEST_CODE = 123;
    public static int APP_RESULT_CODE = 682;

}

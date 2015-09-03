package liubaoyua.customtext.app;

import java.util.List;

import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.utils.Utils;

/**
 * Created by liubaoyua on 2015/9/3.
 */
public class AppHelper {

    public static MyApplication getApplication(){
        return MyApplication.getInstance();
    }

    public static List<AppInfo> getAllList(){
        return getApplication().getAllList();
    }

    public static List<AppInfo> getRecentList(){
        return Utils.getRecentList(getAllList());
    }

    public static void setAllList(List<AppInfo> allList){
        getApplication().setAllList(allList);
    }

    public static void terminal(){
        getApplication().onTerminate();
    }
}

package liubaoyua.customtext.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.ui.AppListActivity;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.DBManager;
import liubaoyua.customtext.utils.PicassoTools;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by kazzy on 2015/7/14 0014.
 * debug test
 */
public class MyApplication extends Application {


    private static final String LOG_NAME = "crash_log_" + getCurrentDateString() + ".txt";
    public static File prefsDir = new File(Environment.getDataDirectory() + "/data/" + Common.PACKAGE_NAME + "/shared_prefs");
    public static File backupDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Common.BACKUP_DIR);
    private static MyApplication application;
    private ArrayList<Activity> list = new ArrayList<>();

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            writeErrorLog(ex);
            Intent intent = new Intent(getApplicationContext(),
                    AppListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            exit();
        }
    };
    private List<AppInfo> allList = new ArrayList<>();

    /**
     * 获取当前日期
     *
     * @return
     */
    private static String getCurrentDateString() {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss",
                Locale.getDefault());
        Date nowDate = new Date();
        result = sdf.format(nowDate);
        return result;
    }

    public static MyApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Common.CRASH_LOG) {
            Thread.setDefaultUncaughtExceptionHandler(handler);
        } else {
            clearCrashLog();
        }
        application = this;
        PicassoTools.init(getApplicationContext());

    }

    /**
     * 打印错误日志
     */
    protected void writeErrorLog(Throwable ex) {
        String info = null;
        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;
        try {
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            ex.printStackTrace(printStream);
            byte[] data = baos.toByteArray();
            info = new String(data);
            data = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (printStream != null) {
                    printStream.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("example", "崩溃信息\n" + info);
        File dir = new File(AppHelper.EXTERNAL_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, LOG_NAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(info.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Activity关闭时，删除Activity列表中的Activity对象
     */
    public void removeActivity(Activity a) {
        list.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象
     */
    public void addActivity(Activity a) {
        list.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity
     */
    public void exit() {
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
        // 杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onTerminate() {
        PicassoTools.destroy();
        DBManager.getInstance(getApplicationContext()).close();
        super.onTerminate();
        System.exit(0);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        PicassoTools.clearCache();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        PicassoTools.clearCache();
    }

    protected List<AppInfo> getAllList() {
        return allList;
    }

    protected void setAllList(List<AppInfo> allList) {
        this.allList = allList;
    }


    private void clearCrashLog() {
        final String prefix = "crash_log";
        final File file = new File(AppHelper.EXTERNAL_DIR);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        String[] logList = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(prefix) && (new File(dir, filename).isFile());
            }
        });

        for (String log : logList) {
            new File(file, log).delete();
        }
    }

}

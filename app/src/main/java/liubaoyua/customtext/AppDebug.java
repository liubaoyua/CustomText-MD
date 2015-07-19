package liubaoyua.customtext;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import liubaoyua.customtext.ui.AppList;
import liubaoyua.customtext.utils.Common;

/**
 * Created by kazzy on 2015/7/14 0014.
 * debug test
 */
public class AppDebug extends Application {
    private static final String LOG_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/Custom Text/";
    private static final String LOG_NAME = "crash_log_" + getCurrentDateString() + ".txt";
    private ArrayList<Activity> list = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        if(!Common.DEBUG){
           Thread.setDefaultUncaughtExceptionHandler(handler);
        }
    }

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            writeErrorLog(ex);
            Intent intent = new Intent(getApplicationContext(),
                    AppList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            exit();
        }
    };

    /**
     * 打印错误日志
     *
     * @param ex
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
        File dir = new File(LOG_DIR);
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
}
//    private RefWatcher mRefWatcher;

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d(Common.TAG,"RefWatcher start");
//        mRefWatcher = LeakCanary.install(this);
//    }
//
//}
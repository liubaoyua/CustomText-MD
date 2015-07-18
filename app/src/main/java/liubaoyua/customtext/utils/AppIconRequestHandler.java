package liubaoyua.customtext.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AppIconRequestHandler extends RequestHandler {

    /** Uri scheme for app icons */
    public static final String SCHEME_PACKAGE_NAME = "packageName";
    private PackageManager mPackageManager;
    private File cacheDir;

    public AppIconRequestHandler(Context context) {
        mPackageManager = context.getPackageManager();
        cacheDir = context.getCacheDir();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        // only handle Uris matching our scheme
        return (SCHEME_PACKAGE_NAME.equals(data.uri.getScheme()));
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {

        Drawable drawable = null;
        Bitmap bitmap = null;
        String schemeStr = request.uri.getScheme();
        String packageName = request.uri.toString().replace(SCHEME_PACKAGE_NAME+":", "");
        if(SCHEME_PACKAGE_NAME.equals(schemeStr)){
            File file = new File(cacheDir,packageName + ".png");
            if(file.exists()){
                bitmap = BitmapFactory.decodeFile(file.toString());
            }else{
                try {
                    drawable = mPackageManager.getApplicationIcon(packageName);
                } catch (PackageManager.NameNotFoundException ignored) {
                    ignored.printStackTrace();
                }
                if (drawable != null) {
                    bitmap =  DrawableUtils.drawableToBitmap(drawable);
//                    //bitmap = ((BitmapDrawable) drawable).getBitmap();
//                    Bitmap bmp = (((BitmapDrawable)drawable).getBitmap());
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.flush();
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
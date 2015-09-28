package liubaoyua.customtext.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

public class IconLoader extends RequestHandler {

    /**
     * Uri scheme for app icons
     */
    public static final String SCHEME_PACKAGE_NAME = "packageName";
    private PackageManager mPackageManager;

    public IconLoader(Context context) {
        mPackageManager = context.getPackageManager();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        // only handle Uris matching our scheme
        return (SCHEME_PACKAGE_NAME.equals(data.uri.getScheme()));
    }

    @Override
    public Result load(Request request, int networkPolicy) {
        Drawable drawable = null;
        Bitmap bitmap = null;
        String schemeStr = request.uri.getScheme();
        String packageName = request.uri.getSchemeSpecificPart();
        if (SCHEME_PACKAGE_NAME.equals(schemeStr)) {
            try {
                drawable = mPackageManager.getApplicationIcon(packageName);
            } catch (Throwable e) {
                drawable = null;
            }
            if (drawable != null) {
                bitmap = Utils.drawableToBitmap(drawable);
            }
        }
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
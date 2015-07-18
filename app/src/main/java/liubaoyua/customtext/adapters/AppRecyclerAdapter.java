package liubaoyua.customtext.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import liubaoyua.customtext.utils.AppIconRequestHandler;
import liubaoyua.customtext.utils.AppInfo;
import liubaoyua.customtext.R;
import liubaoyua.customtext.utils.AppListFilter;
import liubaoyua.customtext.utils.PicassoTools;

/**
 * Created by liubaoyua on 2015/6/20 0020.
 */
public class AppRecyclerAdapter extends RecyclerView.Adapter<AppRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<AppInfo> appList;
    private AppListFilter filter;
    private OnItemClickListener listener;
    private Drawable icon;


    public AppListFilter getFilter() {
        return filter;
    }

    public void setFilter(AppListFilter filter) {
        this.filter = filter;
    }

    public AppRecyclerAdapter(Context mContext, List<AppInfo> appList) {
        this.mContext = mContext;
        this.appList = appList;
        filter = new AppListFilter(this,appList);
        icon = mContext.getResources().getDrawable(R.mipmap.ic_default);
    }

    @Override
    public AppRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_card_app, parent, false);
        return new ViewHolder(view,listener);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final AppRecyclerAdapter.ViewHolder holder, int position) {
        final AppInfo temp = appList.get(position);
        holder.appNameView.setText(temp.appName);
        holder.packageNameView.setText(temp.packageName);
        if(temp.state == AppInfo.ENABLED){
            holder.appNameView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
            holder.packageNameView.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
        }else{
            holder.appNameView.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.packageNameView.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }
//        holder.iconView.setImageDrawable(icon);
//        if(holder.imageLoader != null){
//            holder.imageLoader.cancel(true);
//        }

        PicassoTools.getInstance()
                .load(AppIconRequestHandler.SCHEME_PACKAGE_NAME + ":" + temp.packageName)
//                .resize(48, 48)
//                .centerCrop()
                .noFade().into(holder);
//        holder.imageLoader = new AsyncTask<String, Void, Drawable>() {
//
//            @Override
//            protected Drawable doInBackground(String... params) {
//                String packageName = params[0];
//                File file = new File(mContext.getCacheDir(),packageName +".png");
//                if (file.exists()){
////                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                    return new BitmapDrawable(mContext.getResources(),file.toString());
//                }else{
//                    Drawable drawable = null;
//                    try{
//                        drawable = mContext.getPackageManager().getApplicationIcon(packageName);
//                    }catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    if(drawable != null){
//                        Bitmap bmp = (((BitmapDrawable)drawable).getBitmap());
//                        try {
//                            FileOutputStream out = new FileOutputStream(file);
//                            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
//                            out.flush();
//                            out.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    return drawable;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Drawable result) {
//                if(result != null){
//                    holder.iconView.setImageDrawable(result);
//                }
//            }
//        }.execute(temp.packageName);

//        try {
//            holder.iconView.setImageDrawable(mContext.getPackageManager().
//                    getApplicationIcon(temp.packageName));
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public int getItemCount() {
        return appList == null?0:appList.size();
    }

    public List<AppInfo> getAppList() {
        return appList;
    }

    public void setAppList(List<AppInfo> appList) {
        this.appList = appList;
    }

    /**
     * 内部接口回调方法
     */
    public interface OnItemClickListener {
        void onItemClick(int position, String packageName);
    }

    /**
     * 设置监听方法
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements Target {
        public final View mView;
        public TextView appNameView;
        public TextView packageNameView;
        public ImageView iconView;
//        AsyncTask<String, Void, Drawable> imageLoader;

        public ViewHolder(final View view, final OnItemClickListener listener) {
            super(view);
            mView = view;
            appNameView = (TextView)mView.findViewById(R.id.app_name);
            packageNameView = (TextView)mView.findViewById(R.id.package_name);
            iconView = (ImageView)mView.findViewById(R.id.image_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationZ", 20, 0);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            listener.onItemClick(getAdapterPosition(), appList.get(getAdapterPosition()).packageName);
                        }
                    });
                    animator.start();
                }
            });
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            iconView.setImageDrawable(icon);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            iconView.setImageBitmap(bitmap);
        }
    }
}


